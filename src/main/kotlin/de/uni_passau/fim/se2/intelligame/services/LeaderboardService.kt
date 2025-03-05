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
import de.uni_passau.fim.se2.intelligame.components.AchievementToolWindow
import de.uni_passau.fim.se2.intelligame.leaderboard.Leaderboard
import de.uni_passau.fim.se2.intelligame.leaderboard.command.*
import de.uni_passau.fim.se2.intelligame.util.Logger
import okhttp3.*
import java.util.*

class LeaderboardService(val project: Project): Disposable {
    private val httpClient = OkHttpClient()
    private val gson = Gson()
    private lateinit var webSocketClient: WebSocket
    private var isWebSocketConnected = false
    private var webSocketState = "Disconnected"
    private var userUUID = UUID.randomUUID().toString()

    private var url = MyBundle.getMessage("websocketURL")
    private val request = Request.Builder().url(url).build()
    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)

            isWebSocketConnected = true
            setWebSocketState("Connected")
            println("Connected to the WebSocket server.")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            onReceiveMessage(text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)

            isWebSocketConnected = false
            println("Error: ${t.message}")
            setWebSocketState("Error: ${t.message}")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)

            isWebSocketConnected = false
            println("WebSocket closing: $reason")
            setWebSocketState("Disconnecting")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)

            isWebSocketConnected = false
            println("WebSocket closed: $reason")
            setWebSocketState("Disconnected")
        }
    }

    init {
        println("Leaderboard service")

        val properties = PropertiesComponent.getInstance()
        val uuid = properties.getValue("uuid")

        if (uuid == null) {
            properties.setValue("uuid", userUUID)
        }else{
            userUUID = uuid
        }

        println("userUUID $userUUID")

        connectToWebSocket()
    }

    fun addPoints(pointsToAdd: Int, achievementName: String){
        webSocketClient.send(gson.toJson(AddPointCommand("addPoints", AddPointCommandData(userUUID, pointsToAdd, achievementName))))
    }

    private fun connectToWebSocket(){
        println("Connect to $url")

        if(isWebSocketConnected){
            webSocketClient.close(4000, "Reconnection wanted")
        }

        webSocketClient = httpClient.newWebSocket(request, webSocketListener)
    }

    fun reconnect(){
        connectToWebSocket()
    }

    fun getWebSocketState() : String{
        return webSocketState
    }

    private fun setWebSocketState(state: String){
        println("Web socket state : $state")
        webSocketState = state

        AchievementToolWindow.refresh()
    }

    private fun onReceiveMessage(message: String){
        println("Received message: $message")
        val command = gson.fromJson(message, Command.Default::class.java)
        println("Command action : " + command.action)

        if(command.action == "initUsers"){
            val initUsersCommand = gson.fromJson(message, UserData.InitUsers::class.java)
            Leaderboard.setUsers(initUsersCommand.payload)
            AchievementToolWindow.refresh()
        }else if(command.action == "onUserPointsUpdated"){
            val onUserPointsUpdatedCommand = gson.fromJson(message, OnUserPointsUpdatedCommand::class.java)
            val data = onUserPointsUpdatedCommand.payload
            val user = data.user

            if(user.id == userUUID){
                showNotification("You have earned ${data.earnedPoints} points.")
            }

            Leaderboard.updateUser(user)
            AchievementToolWindow.refresh()
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
