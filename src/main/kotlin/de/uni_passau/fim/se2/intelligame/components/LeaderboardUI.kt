package de.uni_passau.fim.se2.intelligame.components

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBEmptyBorder
import de.uni_passau.fim.se2.intelligame.leaderboard.Leaderboard
import de.uni_passau.fim.se2.intelligame.services.LeaderboardService
import de.uni_passau.fim.se2.intelligame.util.WebSocketState
import jdk.nashorn.tools.ShellFunctions.input
import java.awt.*
import java.awt.event.ActionListener
import javax.swing.*
import javax.swing.event.ChangeListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel


class LeaderboardUI {
    companion object {
        private var inputText: String = ""

        fun create(panel: JPanel, project: Project) {
            val leaderboardService = project.service<LeaderboardService>()

            val properties = PropertiesComponent.getInstance()
            val userUUID = properties.getValue("uuid")!!

            val usernamePanel = JPanel()
            usernamePanel.border = JBEmptyBorder(0, 0, 10, 0)
            usernamePanel.layout = BoxLayout(usernamePanel, BoxLayout.LINE_AXIS)

            val label = JLabel("Username :")
            label.border = JBEmptyBorder(0, 0, 0, 10)
            usernamePanel.add(label)

            val textField = JBTextField()
            textField.text = inputText
            println("Set default username textfield value to $inputText")

            textField.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    inputText = textField.text
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    inputText = textField.text
                }

                override fun changedUpdate(e: DocumentEvent?) {

                }
            })

            usernamePanel.add(textField)

            val validateButton = JButton("Validate")
            usernamePanel.add(validateButton)
            validateButton.addActionListener {leaderboardService.setUsername(textField.text)}

            panel.add(usernamePanel, BorderLayout.NORTH)

            val model = DefaultTableModel(arrayOf(), arrayOf("#", "User", "Points"))
            val users = Leaderboard.getUsers()

            val usersTable: JTable = object : JTable(model) {
                override fun isCellEditable(row: Int, column: Int): Boolean {
                    return false
                }
            }

            usersTable.rowHeight = 30
            usersTable.setAutoCreateRowSorter(true);

            for(iUser in users.indices){
                model.addRow(arrayOf(iUser + 1, users[iUser].name, users[iUser].points))
            }

            val currentUserIndex = users.indexOfFirst { it.id == userUUID }
            val renderer = object : DefaultTableCellRenderer() {
                override fun getTableCellRendererComponent(
                    table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
                ): Component {
                    val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                    component.font = if (row == currentUserIndex) component.font.deriveFont(Font.BOLD) else component.font.deriveFont(
                        Font.PLAIN)
                    return component
                }
            }

            for (i in 0 until usersTable.columnCount) {
                usersTable.columnModel.getColumn(i).cellRenderer = renderer
            }

            val centerRenderer = DefaultTableCellRenderer()
            centerRenderer.horizontalAlignment = JLabel.CENTER

            val firstColumn = usersTable.columnModel.getColumn(0)
            firstColumn.preferredWidth = 15
            firstColumn.setCellRenderer(centerRenderer)

            firstColumn.headerRenderer = centerRenderer

            usersTable.autoResizeMode = JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS // Auto resize

            val scrollPane = JBScrollPane(usersTable)
            scrollPane.setBorder(BorderFactory.createEmptyBorder())
            scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT)
            panel.add(scrollPane, BorderLayout.CENTER)

            val webSocketState = leaderboardService.getWebSocketState()
            val isWebSocketConnected = webSocketState == WebSocketState.CONNECTED

            val statePanel = JPanel()

            val stateLabel = JBLabel("State : $webSocketState")
            if(!isWebSocketConnected){
                stateLabel.foreground = JBColor.RED
            }

            statePanel.add(stateLabel)

            val reconnectButton = JButton("Reconnect")
            reconnectButton.isEnabled = !isWebSocketConnected
            reconnectButton.addActionListener {leaderboardService.reconnect()}
            statePanel.add(reconnectButton)

            panel.add(statePanel, BorderLayout.SOUTH)
        }
    }
}