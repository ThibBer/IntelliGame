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

    private var url = MyBundle.getMessage("websocketURL")
    private val request = Request.Builder().url(url).build()
    private val webSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)

            setWebSocketState(WebSocketState.CONNECTED)
            println("Connected to the WebSocket server.")
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

        val properties = PropertiesComponent.getInstance()

        if (properties.isValueSet("uuid")) {
            userUUID = properties.getValue("uuid")!!
            println("Uuid already set : $userUUID")
        }else{
            userUUID = UUID.randomUUID().toString()
            properties.setValue("uuid", userUUID)
            println("New uuid set : $userUUID")
        }

        println("userUUID $userUUID")
        connectToWebSocket()
    }

    fun addPoints(pointsToAdd: Int, achievementName: String){
        webSocketClient.send(gson.toJson(AddPointCommand("addPoints", AddPointCommandData(userUUID, pointsToAdd, achievementName))))
    }

    private fun connectToWebSocket(){
        println("Connect to $url")

        if(webSocketState == WebSocketState.CONNECTED){
            webSocketClient.close(4000, "Reconnection wanted")
        }

        setWebSocketState(WebSocketState.CONNECTING)

        webSocketClient = httpClient.newWebSocket(request, webSocketListener)
    }

    fun reconnect(){
        connectToWebSocket()
    }

    fun disconnect(){
        if(webSocketState == WebSocketState.CONNECTED){
            webSocketClient.close(4001, "User require disconnecting")
        }
    }

    fun getWebSocketState() : WebSocketState{
        return webSocketState
    }

    fun setUsername(username: String){
        this.username = username
        webSocketClient.send(
            gson.toJson(UpdateUsernameCommand(
        "updateUsername",
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
        println("Command action : " + command.action)

        if(command.action == "initUsers"){
            val initUsersCommand = gson.fromJson(message, UserData.InitUsers::class.java)
            Leaderboard.setUsers(initUsersCommand.payload)
            GamificationToolWindow.refresh()
        }else if(command.action == "onUserPointsUpdated"){
            val onUserPointsUpdatedCommand = gson.fromJson(message, OnUserPointsUpdatedCommand::class.java)
            val data = onUserPointsUpdatedCommand.payload
            val user = data.user

            if(user.id == userUUID){
                showNotification("You have earned ${data.earnedPoints} points.")
            }

            Leaderboard.updateUser(user)
            GamificationToolWindow.refresh()
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
