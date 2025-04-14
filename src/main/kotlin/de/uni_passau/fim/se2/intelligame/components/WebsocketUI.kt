package de.uni_passau.fim.se2.intelligame.components

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import de.uni_passau.fim.se2.intelligame.services.GamificationService
import de.uni_passau.fim.se2.intelligame.util.WebSocketState
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JPanel

class WebsocketUI {
    companion object {
        fun create(project: Project): JPanel {
            val gamificationService = project.service<GamificationService>()
            val webSocketState = gamificationService.getWebSocketState()
            val isWebSocketConnected = webSocketState == WebSocketState.CONNECTED

            val panel = JPanel(GridBagLayout())
            val gbc = GridBagConstraints()
            gbc.gridx = GridBagConstraints.RELATIVE
            gbc.gridy = 0
            gbc.anchor = GridBagConstraints.CENTER

            val stateLabel = JBLabel("State : $webSocketState")
            if(!isWebSocketConnected){
                stateLabel.foreground = JBColor.RED
            }

            gbc.insets = JBUI.insets(0, 0, 0, 5)
            panel.add(stateLabel, gbc)

            val connectionButton = JButton(if(isWebSocketConnected) "Disconnect" else "Connect")
            connectionButton.addActionListener {
                if(isWebSocketConnected) {
                    gamificationService.disconnect()
                }else{
                    gamificationService.connect()
                }
            }

            gbc.insets = JBUI.insets(0, 0, 0, 0)
            panel.add(connectionButton, gbc)

            return panel
        }
    }
}