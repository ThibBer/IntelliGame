package de.uni_passau.fim.se2.intelligame.components

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import de.uni_passau.fim.se2.intelligame.services.GamificationService
import de.uni_passau.fim.se2.intelligame.util.WebSocketState
import java.awt.Component
import javax.swing.JButton
import javax.swing.JPanel

class WebsocketUI {
    companion object {
        fun create(project: Project): JPanel {
            val gamificationService = project.service<GamificationService>()
            val webSocketState = gamificationService.getWebSocketState()
            val isWebSocketConnected = webSocketState == WebSocketState.CONNECTED

            val panel = JPanel()
            panel.alignmentX = Component.CENTER_ALIGNMENT

            val stateLabel = JBLabel("State : $webSocketState")
            if(!isWebSocketConnected){
                stateLabel.foreground = JBColor.RED
            }

            panel.add(stateLabel)

            val connectionButton = JButton(if(isWebSocketConnected) "Disconnect" else "Connect")
            connectionButton.addActionListener {
                if(isWebSocketConnected) {
                    gamificationService.disconnect()
                }else{
                    gamificationService.connect()
                }
            }

            panel.add(connectionButton)

            return panel
        }
    }
}