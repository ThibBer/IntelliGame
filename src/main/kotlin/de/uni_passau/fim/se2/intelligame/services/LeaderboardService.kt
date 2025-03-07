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
import com.intellij.ide.util.PropertiesComponent
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import de.uni_passau.fim.se2.intelligame.MyBundle
import de.uni_passau.fim.se2.intelligame.components.GamificationToolWindow
import de.uni_passau.fim.se2.intelligame.leaderboard.Leaderboard
import de.uni_passau.fim.se2.intelligame.leaderboard.command.*
import de.uni_passau.fim.se2.intelligame.util.Logger
import de.uni_passau.fim.se2.intelligame.util.Util
import de.uni_passau.fim.se2.intelligame.util.WebSocketState
import okhttp3.*
import java.util.*

class LeaderboardService(val project: Project): Disposable {
    private val httpClient = OkHttpClient()
    private val gson = Gson()
    private lateinit var webSocketClient: WebSocket
    private var webSocketState = WebSocketState.DISCONNECTED
    private var userUUID = ""
    private var username = ""
    private val properties = PropertiesComponent.getInstance()

    private var url = MyBundle.getMessage("websocketURL")
    private val request = Request.Builder().url(url).build()
    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)

            setWebSocketState(WebSocketState.CONNECTED)
            println("Connected to the WebSocket server.")

            webSocket.send(gson.toJson(
                UserConnectedCommand(
                    UserConnectedCommandData(userUUID, username)
                )
            ))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            onReceiveMessage(text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)

            println("Error: ${t.message}")
            setWebSocketState(WebSocketState.ERROR)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)

            println("WebSocket closing: $reason")
            setWebSocketState(WebSocketState.DISCONNECTING)
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)

            println("WebSocket closed: $reason")
            setWebSocketState(WebSocketState.DISCONNECTED)
        }
    }

    init {
        println("Leaderboard service")

        userUUID = getPropertyValue("uuid", UUID.randomUUID().toString())
        username = getPropertyValue("username", Util.generatePseudo())

        connectToWebSocket()
    }

    private fun getPropertyValue(key: String, defaultValue: String): String{
        if (properties.isValueSet(key)) {
            return properties.getValue(key)!!
        }

        properties.setValue(key, defaultValue)
        return defaultValue
    }

    fun addPoints(pointsToAdd: Int, achievementName: String){
        webSocketClient.send(gson.toJson(
            AddPointsCommand(
                AddPointsCommandData(userUUID, pointsToAdd, achievementName)
            )
        ))
    }

    private fun connectToWebSocket(){
        println("Connect to $url")

        disconnect()

        setWebSocketState(WebSocketState.CONNECTING)

        webSocketClient = httpClient.newWebSocket(request, webSocketListener)
    }

    fun reconnect(){
        connectToWebSocket()
    }

    private fun disconnect(){
        if(webSocketState == WebSocketState.CONNECTED){
            webSocketClient.close(4001, "User require disconnecting")
        }
    }

    fun getWebSocketState() : WebSocketState{
        return webSocketState
    }

    fun setUsername(username: String){
        this.username = username
        properties.setValue("username", username)

        webSocketClient.send(
            gson.toJson(UpdateUsernameCommand(
                UpdateUsernameCommandData(userUUID, username)
            ))
        )
    }

    fun getUsername(): String{
        return username
    }

    private fun setWebSocketState(state: WebSocketState){
        println("Web socket state : $state")
        webSocketState = state

        GamificationToolWindow.refresh()
    }

    private fun onReceiveMessage(message: String){
        println("Received message: $message")
        val command = gson.fromJson(message, Command.Default::class.java)

        when (command.action) {
            "onInitUsers" -> {
                val initUsersCommand = gson.fromJson(message, UserData.InitUsers::class.java)
                Leaderboard.setUsers(initUsersCommand.payload)
                GamificationToolWindow.refresh()
            }
            "onUserPointsUpdated" -> {
                val onUserPointsUpdatedCommand = gson.fromJson(message, OnUserPointsUpdatedCommand::class.java)
                val data = onUserPointsUpdatedCommand.payload
                val user = data.user

                if(user.id == userUUID){
                    showNotification("You have earned ${data.earnedPoints} points.")
                }

                Leaderboard.updateUser(user)
                GamificationToolWindow.refresh()
            }
            "onUserAdded" -> {
                val onUserAddedCommand = gson.fromJson(message, OnUserAddedCommand::class.java)

                Leaderboard.addUser(onUserAddedCommand.payload)
                GamificationToolWindow.refresh()
            }
            "onUsernameUpdated" -> {
                val onUsernameUpdatedCommand = gson.fromJson(message, OnUsernameUpdatedCommand::class.java)

                Leaderboard.updateUser(onUsernameUpdatedCommand.payload)
                GamificationToolWindow.refresh()
            }
        }
    }

    private fun showNotification(message: String) {
        val notification = NotificationGroupManager.getInstance().getNotificationGroup("Gamification").createNotification(
            message,
            NotificationType.INFORMATION
        )

        notification.notify(null)

        Logger.logStatus(message, Logger.Kind.Notification, project)
    }

    override fun dispose() = Unit
}
