package de.uni_passau.fim.se2.intelligame.components

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import de.uni_passau.fim.se2.intelligame.util.GameMode
import de.uni_passau.fim.se2.intelligame.util.Util
import java.awt.BorderLayout
import javax.swing.*
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableRowSorter

class TablePopupDialog(project: Project?) : DialogWrapper(project) {
    init {
        title = "Leaderboard Actions To Win Points"
        init()
        setSize(500, 400)
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout())
        val columnNames = arrayOf("Name", "Description")

        val tableModel = DefaultTableModel(arrayOf(), columnNames)
        for(achievement in Util.getAchievements().filter { it.supportsGameMode(GameMode.LEADERBOARD) }){
            tableModel.addRow(arrayOf<Any>(achievement.getName(), achievement.getDescription().replace(" X ", " ")))
        }

        val table: JTable = object : JTable(tableModel) {
            override fun isCellEditable(row: Int, column: Int): Boolean {
                return false
            }
        }

        val sorter = TableRowSorter(tableModel)
        table.setRowSorter(sorter)

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.setCellSelectionEnabled(false)

        table.rowHeight = 30
        table.fillsViewportHeight = true

        val scrollPane = JBScrollPane(table)
        panel.add(scrollPane, BorderLayout.CENTER)

        return panel
    }

    override fun createActions(): Array<Action> {
        return arrayOf(object : DialogWrapperExitAction("Close", CLOSE_EXIT_CODE) {})
    }
}
