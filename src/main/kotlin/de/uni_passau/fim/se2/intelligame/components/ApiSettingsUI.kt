package de.uni_passau.fim.se2.intelligame.components

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBEmptyBorder
import de.uni_passau.fim.se2.intelligame.services.GamificationService
import java.awt.BorderLayout
import javax.swing.*

class ApiSettingsUI {
    companion object {
        fun create(project: Project): JComponent {
            val gamificationService = project.service<GamificationService>()

            val apiSettingsPanel = JPanel(BorderLayout())

            val topPanel = JPanel()
            topPanel.layout = BoxLayout(topPanel, BoxLayout.PAGE_AXIS)

            val panel = JPanel()
            panel.border = JBEmptyBorder(10)
            panel.layout = BoxLayout(panel, BoxLayout.LINE_AXIS)

            val label = JLabel("API key :")
            label.border = JBEmptyBorder(0, 0, 0, 10)
            panel.add(label)

            val textField = JBTextField()
            panel.add(textField)

            val validateButton = JButton("Validate")
            validateButton.addActionListener {gamificationService.tryApiKey(textField.text)}
            panel.add(validateButton)

            topPanel.add(panel)

            val stateLabel = JLabel("State : ${gamificationService.getWebSocketState()}")
            topPanel.add(stateLabel)

            apiSettingsPanel.add(topPanel, BorderLayout.NORTH)

            return apiSettingsPanel
        }
    }
}