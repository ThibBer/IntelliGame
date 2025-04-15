/*
 * Copyright 2023 IntelliGame contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.uni_passau.fim.se2.intelligame.services

import com.google.gson.Gson
import com.intellij.ide.DataManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.EditorNotifications
import com.intellij.ui.content.ContentFactory
import de.uni_passau.fim.se2.intelligame.MyBundle
import de.uni_passau.fim.se2.intelligame.achievements.Achievement
import de.uni_passau.fim.se2.intelligame.command.*
import de.uni_passau.fim.se2.intelligame.components.WindowPanel
import de.uni_passau.fim.se2.intelligame.leaderboard.Leaderboard
import de.uni_passau.fim.se2.intelligame.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import kotlin.reflect.KClass

@Service(Service.Level.PROJECT)
class GamificationService(val project: Project) : Disposable {
    private val httpClient = OkHttpClient()
    private val gson = Gson()
    private lateinit var webSocketClient: WebSocket
    private var webSocketState = WebSocketState.DISCONNECTED
    private val properties = PropertiesComponent.getInstance()
    private var gameMode: GameMode = GameMode.valueOf(
        properties.getValue("gamification-game-mode", GameMode.LEADERBOARD.name)
    )

    private var userId = ""
    private var username = ""
    private var apiKey = ""
    private val csvPath = Util.getEvaluationFilePath(project, "Actions.csv")
    private val actionCSV = CSVFile(listOf("Action", "Name", "Points", "GameMode", "Timestamp"))

    private var webSocketUrl = MyBundle.getMessage("websocketURL")
    private var apiUrl = MyBundle.getMessage("apiURL")

    private val heartbeatInterval = MyBundle.getMessage("heartbeatInterval", "30000").toLong()

    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)

            setWebSocketState(WebSocketState.CONNECTED)

            properties.setValue("gamification-api-key", apiKey)

            webSocket.send(
                gson.toJson(
                    UserConnectedCommand(
                        UserConnectedCommandData(userId, username)
                    )
                )
            )

            GlobalScope.launch {
                webSocketHeartbeat()
            }

            Logger.logStatus("Websocket connection opened", Logger.Kind.Debug, project)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            onReceiveMessage(text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)

            Logger.logStatus("Websocket failure : ${t.message}", Logger.Kind.Error, project)
            setWebSocketState(WebSocketState.ERROR)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)

            Logger.logStatus("Websocket closing : $reason | code : $code", Logger.Kind.Debug, project)

            if (code != 1008) {
                setWebSocketState(WebSocketState.DISCONNECTING)
            } else {
                properties.unsetValue("gamification-api-key")
                apiKey = ""
                setWebSocketState(WebSocketState.INVALID_API_KEY)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)

            Logger.logStatus("Websocket closed : $reason | code : $code", Logger.Kind.Debug, project)
            setWebSocketState(WebSocketState.DISCONNECTED)
        }
    }

    init {
        println("Leaderboard service")

        userId = getPropertyValue("gamification-user-id", UUID.randomUUID().toString())
        username = getPropertyValue("gamification-username", Util.generatePseudo())
        apiKey = getPropertyValue("gamification-api-key", "")

        connect()
    }

    private fun refresh(){
        val project = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(10, TimeUnit.SECONDS)!!.getData(PlatformDataKeys.PROJECT)
        if(project == null){
            thisLogger().debug("Project is null, cannot refresh the window")
            return
        }

        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.getToolWindow("Gamification")
        if(toolWindow == null){
            thisLogger().debug("Tool window is null, cannot refresh the window")
            return
        }

        SwingUtilities.invokeLater {
            toolWindow.contentManager.removeAllContents(true)
            val panel = WindowPanel(project).create()
            val content = ContentFactory.getInstance().createContent(panel, null, false)
            toolWindow.contentManager.addContent(content)
        }
    }

    private suspend fun webSocketHeartbeat() {
        while (webSocketState == WebSocketState.CONNECTED) {
            webSocketClient.send("heartbeat")
            delay(heartbeatInterval)
        }
    }

    private fun getPropertyValue(key: String, defaultValue: String): String {
        if (properties.isValueSet(key)) {
            return properties.getValue(key)!!
        }

        properties.setValue(key, defaultValue)
        return defaultValue
    }

    fun addPoints(pointsToAdd: Int, achievementClass: KClass<out Achievement>) {
        val className = achievementClass.simpleName!!

        webSocketClient.send(
            gson.toJson(
                AddActivityCommand(
                    AddActivityCommandData(userId, pointsToAdd, className, gameMode.ordinal)
                )
            )
        )

        val instance = achievementClass.objectInstance!!
        actionCSV.appendLine(
            listOf(
                className,
                instance.getName(),
                pointsToAdd.toString(),
                gameMode.name,
                Timestamp(System.currentTimeMillis()).toString()
            )
        )

        actionCSV.save(csvPath)
        EditorNotifications.getInstance(project).updateAllNotifications()
    }

    fun connect() {
        Logger.logStatus("Connect to $webSocketUrl", Logger.Kind.Debug, project)

        disconnect()

        setWebSocketState(WebSocketState.CONNECTING)

        val request = Request.Builder().url(webSocketUrl).addHeader("API-KEY", apiKey).build()
        webSocketClient = httpClient.newWebSocket(request, webSocketListener)
    }

    fun disconnect() {
        if (webSocketState == WebSocketState.CONNECTED) {
            webSocketClient.close(4001, "User require disconnecting")
        }
    }

    fun getWebSocketState(): WebSocketState {
        return webSocketState
    }

    fun setUsername(username: String) {
        webSocketClient.send(
            gson.toJson(
                UpdateUsernameCommand(
                    UpdateUsernameCommandData(userId, username)
                )
            )
        )
    }

    fun tryApiKey(apiKey: String) {
        this.apiKey = apiKey
        connect()
    }

    fun getUsername(): String {
        return username
    }

    private fun setWebSocketState(state: WebSocketState) {
        Logger.logStatus("Web socket state : $state", Logger.Kind.Debug, project)
        webSocketState = state

        refresh()

        if (webSocketState == WebSocketState.CONNECTED) {
            showNotification("Connected to gamification server")
        } else if (webSocketState == WebSocketState.DISCONNECTED) {
            showNotification("Disconnected from gamification server")
        }

        EditorNotifications.getInstance(project).updateAllNotifications()
    }

    private fun onReceiveMessage(message: String) {
        println("Received message: $message")
        val command = gson.fromJson(message, DefaultCommand::class.java)

        when (command.action) {
            "onInitUsers" -> onInitUsers(message)
            "onUserActivityUpdated" -> onUserActivityUpdated(message)
            "onUserAdded" -> onUserAdded(message)
            "onUsernameUpdated" -> onUsernameUpdated(message)
        }
    }

    private fun onInitUsers(message: String) {
        val initUsersCommand = gson.fromJson(message, InitUsersCommand::class.java)
        Leaderboard.setUsers(initUsersCommand.payload)
        refresh()
    }

    private fun onUserActivityUpdated(message: String) {
        val onUserActivityUpdatedCommand = gson.fromJson(message, OnUserActivityUpdatedCommand::class.java)
        val data = onUserActivityUpdatedCommand.payload
        val user = data.user

        if (user.id == userId && gameMode == GameMode.LEADERBOARD) {
            showNotification("You have earned ${data.earnedPoints} points.")
        }

        Leaderboard.updateUser(user)
        refresh()
    }

    private fun onUserAdded(message: String) {
        val onUserAddedCommand = gson.fromJson(message, OnUserAddedCommand::class.java)

        Leaderboard.addUser(onUserAddedCommand.payload)
        refresh()
    }

    private fun onUsernameUpdated(message: String) {
        val onUsernameUpdatedCommand = gson.fromJson(message, OnUsernameUpdatedCommand::class.java)
        val user = onUsernameUpdatedCommand.payload

        if (user.id == userId) {
            username = user.username
            properties.setValue("gamification-username", username)
        }

        Leaderboard.updateUser(user)
        refresh()
    }

    private fun showNotification(message: String) {
        val notification = NotificationGroupManager.getInstance().getNotificationGroup("Gamification").createNotification(
            message,
            NotificationType.INFORMATION
        )

        notification.notify(project)

        Logger.logStatus(message, Logger.Kind.Notification, project)
    }

    fun setGameMode(gameMode: GameMode) {
        if(this.gameMode != gameMode){
            showNotification("Game mode selected : ${gameMode.name.lowercase()}")
        }

        this.gameMode = gameMode
        properties.setValue("gamification-game-mode", gameMode.name)
    }

    fun getGameMode(): GameMode {
        return gameMode
    }

    fun resetPluginSettings() {
        Logger.logStatus("Resetting plugin settings", Logger.Kind.Debug, project)

        disconnect()

        properties.unsetValue("gamification-game-mode")
        properties.unsetValue("gamification-username")
        properties.unsetValue("gamification-api-key")

        properties.unsetValue("gamification-user-id")

        userId = UUID.randomUUID().toString()
        properties.setValue("gamification-user-id", userId)

        for(achievement in Util.getAchievements()){
            properties.unsetValue(achievement.getPropertyKey())
            properties.unsetValue(achievement.getLevelPropertyKey())
        }
    }

    fun sendExperimentData(files: List<File>, canSendTestFiles: Boolean, canSendSourceFiles: Boolean, callback: ((Int?) -> Unit)? = null) {
        if (files.isEmpty() && callback != null) {
            callback(null)
            return
        }

        val client = httpClient.newBuilder().build()
        val bodyBuilder  = MultipartBody.Builder().setType(MultipartBody.FORM)
        bodyBuilder.addFormDataPart("userId", userId)

        for (file in files) {
            bodyBuilder.addFormDataPart(
                "file", file.name, file.asRequestBody("application/octet-stream".toMediaType())
            )
        }

        val outputTestZipFile = System.getProperty("java.io.tmpdir") + File.separatorChar + "testClasses.zip"
        if(canSendTestFiles){
            val testsDirectory = project.basePath + "/src/test/java"

            Util.zipFolder(testsDirectory, outputTestZipFile)

            val file = File(outputTestZipFile)
            bodyBuilder.addFormDataPart(
                "tests", file.name, file.asRequestBody("application/zip".toMediaType())
            )
        }

        val outputSourceZipFile = System.getProperty("java.io.tmpdir") + File.separatorChar + "sourceClasses.zip"
        if(canSendSourceFiles){
            val testsDirectory = project.basePath + "/src/main/java"

            Util.zipFolder(testsDirectory, outputSourceZipFile)

            val file = File(outputSourceZipFile)
            bodyBuilder.addFormDataPart(
                "sources", file.name, file.asRequestBody("application/zip".toMediaType())
            )
        }

        val url = "${apiUrl}/experimentData"
        val requestBody = bodyBuilder.build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("content-type", "multipart/form-data")
            .addHeader("API-KEY", apiKey)
            .build()

        val stringBuilder = StringBuilder("POST ${files.size} files to $url :")
        files.forEach { stringBuilder.appendLine("- ${it.name} (${it.length()} bytes)") }

        Logger.logStatus(stringBuilder.toString(), Logger.Kind.Debug, project)

        client.newCall(request).execute().use { response ->
            val zipTestsFile = File(outputTestZipFile)
            zipTestsFile.delete()

            val zipSourceFile = File(outputSourceZipFile)
            zipSourceFile.delete()

            if(response.code == 200) {
                showNotification("File successfully sent, thanks for your help ! \uD83D\uDE09")
            }else{
                showNotification("Error occurred while trying to send files - ${response.code} | ${response.body.string()}")
            }

            if(callback != null){
                callback(response.code)
            }
        }
    }

    fun updateCoverage(coverageInfo: CoverageInfo, testedClass: String, testName: String, project: Project?){
        if(project == null){
            thisLogger().debug("Project is null, cannot update coverage")
            return
        }

        val path = Util.getEvaluationFilePath(project, "Coverage.csv")

        val csvFile = CSVFile(coverageInfo.getAsCsvHeader("testedClass,testName"))
        csvFile.appendLine(coverageInfo.getAsCsvData() + ",$testedClass,$testName")
        csvFile.save(path)

        webSocketClient.send(
            gson.toJson(
                UpdateCoverageCommand(
                    UpdateCoverageCommandData(userId, coverageInfo, testedClass, testName, gameMode.ordinal)
                )
            )
        )
    }

    override fun dispose() = Unit
}
