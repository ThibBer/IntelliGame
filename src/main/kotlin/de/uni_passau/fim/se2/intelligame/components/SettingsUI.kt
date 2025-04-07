package de.uni_passau.fim.se2.intelligame.components

import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.*
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBEmptyBorder
import de.uni_passau.fim.se2.intelligame.services.GamificationService
import de.uni_passau.fim.se2.intelligame.util.Util
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.*
import javax.swing.tree.DefaultTreeModel


class CheckboxTreeRenderer : CheckboxTree.CheckboxTreeCellRenderer() {
    override fun customizeRenderer(tree: JTree?, value: Any?, selected: Boolean, expanded: Boolean, leaf: Boolean, row: Int, hasFocus: Boolean) {
        super.customizeRenderer(tree, value, selected, expanded, leaf, row, hasFocus)

        if (value is CheckedTreeNode) {
            val userObject = value.userObject
            if (userObject is File) {
                textRenderer.append(userObject.name, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            }else if (userObject is String) {
                textRenderer.append(userObject, SimpleTextAttributes.REGULAR_ATTRIBUTES)
            }
        }
    }
}

fun interface OnNodeStateChanged {
    fun nodeStateChanged(node: CheckedTreeNode)
}

class CustomCheckboxTreeListener : CheckboxTreeListener {
    var nodeStateChanged: OnNodeStateChanged? = null

    override fun nodeStateChanged(node: CheckedTreeNode) {
        super.nodeStateChanged(node)
        nodeStateChanged?.nodeStateChanged(node)
    }
}

class SettingsUI {

    companion object{
        private val files = ArrayList<File>()

        fun create(project: Project): JComponent {
            val properties = PropertiesComponent.getInstance()

            val gamificationService = project.service<GamificationService>()
            val settingsPanel = JPanel(BorderLayout())

            val topPanel = JPanel()
            topPanel.layout = BoxLayout(topPanel, BoxLayout.PAGE_AXIS)

            val myIdPanel = JPanel()
            myIdPanel.border = JBEmptyBorder(0, 0, 10, 0)
            myIdPanel.layout = BoxLayout(myIdPanel, BoxLayout.LINE_AXIS)

            val myIdLabel = JLabel("My id :")
            myIdLabel.border = JBEmptyBorder(0, 0, 0, 10)
            myIdPanel.add(myIdLabel)

            val textField = JBTextField()
            textField.text = properties.getValue("gamification-user-id")
            textField.isEditable = false
            myIdPanel.add(textField)

            topPanel.add(myIdPanel)

            val dumpDataPanel = JPanel(BorderLayout())
            dumpDataPanel.border = BorderFactory.createTitledBorder("Dump data")

            val filesData = getCheckedTree(project)
            val root = filesData.first

            files.clear()
            for (file in filesData.second) {
                files.add(file)
            }

            val checkboxTreeListener = CustomCheckboxTreeListener()

            val tree = CheckboxTree(CheckboxTreeRenderer(), root)
            tree.addCheckboxTreeListener(checkboxTreeListener)

            tree.border = BorderFactory.createEmptyBorder(0, 0, 15, 0)
            tree.isRootVisible = true

            val mouseAdapter = object : MouseAdapter() {
                private fun myPopupEvent(e: MouseEvent) {
                    val x: Int = e.x
                    val y: Int = e.y
                    val sourceTree = e.source as JTree
                    val path = tree.getPathForLocation(x, y) ?: return

                    sourceTree.selectionPath = path

                    val popup = JPopupMenu()
                    val reloadFromDiskItem = JMenuItem("Reload from disk")
                    reloadFromDiskItem.icon = AllIcons.Actions.Refresh
                    reloadFromDiskItem.addActionListener {
                        tree.removeAll()
                        tree.model = DefaultTreeModel(getCheckedTree(project).first)
                    }
                    popup.add(reloadFromDiskItem)
                    popup.show(sourceTree, x, y)
                }

                override fun mousePressed(e: MouseEvent) {
                    if (e.isPopupTrigger) {
                        myPopupEvent(e)
                    }
                }

                override fun mouseReleased(e: MouseEvent) {
                    if (e.isPopupTrigger) {
                        myPopupEvent(e)
                    }
                }
            }
            tree.addMouseListener(mouseAdapter)

            dumpDataPanel.add(tree, BorderLayout.NORTH)

            val sendDataButton = JButton("Send experiment data")
            sendDataButton.setHorizontalTextPosition(SwingConstants.LEADING)
            sendDataButton.addActionListener {
                sendDataButton.icon = AnimatedIcon.Default()

                gamificationService.sendExperimentData(files) {
                    sendDataButton.icon = null
                }
            }

            sendDataButton.isEnabled = files.isNotEmpty()

            dumpDataPanel.add(sendDataButton, BorderLayout.CENTER)

            checkboxTreeListener.nodeStateChanged = OnNodeStateChanged {
                if(it.userObject is File) {
                    val file = it.userObject as File

                    if(it.isChecked){
                        files.add(file)
                    }else{
                        files.remove(file)
                    }

                    sendDataButton.isEnabled = files.isNotEmpty()
                }
            }

            topPanel.add(dumpDataPanel)
            settingsPanel.add(topPanel, BorderLayout.NORTH)

            val resetSettingsButton = JButton("Reset plugin settings")
            resetSettingsButton.addActionListener {
                val popup = JBPopupFactory.getInstance().createConfirmation("Are You Sure To Delete Gamification Plugin Settings ?", "Yes", "No", {
                    gamificationService.resetPluginSettings()
                }, 0)

                popup.showCenteredInCurrentWindow(project)
            }

            settingsPanel.add(resetSettingsButton, BorderLayout.SOUTH)

            return settingsPanel
        }

        private fun getFiles(directory: String): List<File>{
            val files = ArrayList<File>()

            for (file in File(directory).walkTopDown()) {
                if(file.isFile){
                    files.add(file)
                }
            }

            return files
        }

        private fun getCheckedTree(project: Project): Pair<CheckedTreeNode, List<File>> {
            val directory = Util.getEvaluationDirectoryPath(project)
            val root = CheckedTreeNode(directory)

            val files = getFiles(directory)
            for(file in files){
                val checkedTreeNode = CheckedTreeNode(file)
                checkedTreeNode.isChecked = true

                root.add(checkedTreeNode)
            }

            return Pair(root, files)
        }
    }
}