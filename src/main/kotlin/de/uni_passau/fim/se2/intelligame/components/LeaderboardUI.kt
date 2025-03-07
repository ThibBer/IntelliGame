package de.uni_passau.fim.se2.intelligame.components

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBEmptyBorder
import de.uni_passau.fim.se2.intelligame.leaderboard.Leaderboard
import de.uni_passau.fim.se2.intelligame.services.LeaderboardService
import de.uni_passau.fim.se2.intelligame.util.WebSocketState
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Font
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableRowSorter




class LeaderboardUI {
    companion object {
        private var usernameInputText: String = ""
        private var searchInputText: String = ""

        fun create(panel: JPanel, project: Project) {
            val leaderboardService = project.service<LeaderboardService>()

            val properties = PropertiesComponent.getInstance()
            val userUUID = properties.getValue("uuid")!!

            val topPanel = JPanel()
            topPanel.border = JBEmptyBorder(0, 0, 10, 0)
            topPanel.layout = BoxLayout(topPanel, BoxLayout.PAGE_AXIS)

            val usernamePanel = JPanel()
            usernamePanel.border = JBEmptyBorder(0, 0, 10, 0)
            usernamePanel.layout = BoxLayout(usernamePanel, BoxLayout.LINE_AXIS)

            val label = JLabel("Username :")
            label.border = JBEmptyBorder(0, 0, 0, 10)
            usernamePanel.add(label)

            val textField = JBTextField()
            textField.text = usernameInputText.ifEmpty { leaderboardService.getUsername() }

            usernamePanel.add(textField)

            val validateButton = JButton("Validate")
            validateButton.addActionListener {leaderboardService.setUsername(textField.text)}
            validateButton.isEnabled = usernameInputText.isNotBlank() && usernameInputText != leaderboardService.getUsername()
            usernamePanel.add(validateButton)

            textField.document.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    onTextChanged()
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    onTextChanged()
                }

                override fun changedUpdate(e: DocumentEvent?) {

                }

                private fun onTextChanged(){
                    usernameInputText = textField.text.trim()
                    validateButton.isEnabled = usernameInputText.isNotBlank() && usernameInputText != leaderboardService.getUsername()
                }
            })

            topPanel.add(usernamePanel)

            val searchPanel = JPanel()
            searchPanel.border = JBEmptyBorder(0, 0, 10, 0)
            searchPanel.layout = BoxLayout(searchPanel, BoxLayout.LINE_AXIS)

            val searchTextField = SearchTextField()
            searchTextField.text = searchInputText

            searchPanel.add(searchTextField)

            topPanel.add(searchPanel)
            panel.add(topPanel, BorderLayout.NORTH)

            val model = DefaultTableModel(arrayOf(), arrayOf("#", "User", "Points"))
            val users = Leaderboard.getUsers()

            val usersTable: JTable = object : JTable(model) {
                override fun isCellEditable(row: Int, column: Int): Boolean {
                    return false
                }
            }

            usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            usersTable.setCellSelectionEnabled(false)

            val sorter = TableRowSorter(model)
            usersTable.setRowSorter(sorter)

            usersTable.rowHeight = 30

            for(iUser in users.indices){
                model.addRow(arrayOf(iUser + 1, users[iUser].username, users[iUser].points))
            }

            val currentUserIndex = users.indexOfFirst { it.id == userUUID }
            val renderer = object : DefaultTableCellRenderer() {
                override fun getTableCellRendererComponent(
                    table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
                ): Component {
                    val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                    component.font = component.font.deriveFont(
                        if (row == currentUserIndex) Font.BOLD else Font.PLAIN
                    )

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

            searchTextField.addDocumentListener(object : DocumentListener {
                override fun insertUpdate(e: DocumentEvent?) {
                    onTextChanged()
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    onTextChanged()
                }

                override fun changedUpdate(e: DocumentEvent?) {}

                private fun onTextChanged(){
                    searchInputText = searchTextField.text.trim()

                    if (searchInputText.isBlank()) {
                        sorter.setRowFilter(null)
                    } else {
                        sorter.setRowFilter(RowFilter.regexFilter("(?i)$searchInputText", 1))
                    }
                }
            })

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