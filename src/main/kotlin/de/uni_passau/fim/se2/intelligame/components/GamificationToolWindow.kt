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

package de.uni_passau.fim.se2.intelligame.components

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.JBColor.isBright
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBEmptyBorder
import de.uni_passau.fim.se2.intelligame.achievements.Achievement.Language
import de.uni_passau.fim.se2.intelligame.util.Util
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Font
import java.util.concurrent.TimeUnit
import javax.swing.*
import javax.swing.table.DefaultTableCellRenderer


class GamificationToolWindow : ToolWindowFactory {
    private val contentFactory = ContentFactory.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = createPanel(project)
        val content = contentFactory.createContent(panel, null, false)

        toolWindow.contentManager.addContent(content)
    }

    companion object {
        fun createPanel(project: Project): JComponent {
            val panel = JPanel(BorderLayout())
            panel.border = JBEmptyBorder(10)

            LeaderboardUI.create(panel, project)

            return panel
        }

        fun refresh() {
            val project = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(10, TimeUnit.SECONDS)!!.getData(PlatformDataKeys.PROJECT)
            val toolWindow = ToolWindowManager.getInstance(project!!).getToolWindow("Gamification")!!

            SwingUtilities.invokeLater {
                toolWindow.contentManager.removeAllContents(true)
                val content = ContentFactory.getInstance().createContent(createPanel(project), null, false)
                toolWindow.contentManager.addContent(content)
            }
        }
    }

    private fun achievementList(): JPanel {
        val panel = panel {
            groupRowsRange("Testing") {
                for (achievement in Util.getTestsAchievement()) {
                    row {
                        when (achievement.getLevel()) {
                            0 -> icon(TrophyIcons.trophyDefaultIcon)
                            1 -> icon(TrophyIcons.trophyBronzeIcon)
                            2 -> if (!isBright()) {
                                icon(TrophyIcons.trophySilverIcon)
                            } else {
                                icon(TrophyIcons.trophySilverLightIcon)
                            }
                            3 -> icon(TrophyIcons.trophyGoldIcon)
                            else -> {
                                icon(TrophyIcons.trophyPlatinIcon)
                            }
                        }
                        label(achievement.getName()).align(AlignX.LEFT)
                        contextHelp(achievement.getDescription(), achievement.getName())
                        if (achievement.supportsLanguages().contains(Language.Java)) {
                            icon(TrophyIcons.javaIcon)
                        } else {
                            cell()
                        }
                        if (achievement.supportsLanguages().contains(Language.JavaScript)) {
                            icon(TrophyIcons.javaScriptIcon)
                        } else {
                            cell()
                        }
                        if (achievement.getLevel() > 3) {
                            val progressBar = JProgressBar(1, 1)
                            progressBar.value = achievement.progress()
                            progressBar.isStringPainted = false
                            cell(progressBar).align(AlignX.RIGHT)
                            label(achievement.progress().toString())
                        } else {
                            val progressBar = JProgressBar(0, achievement.nextStep())
                            progressBar.value = achievement.progress()
                            progressBar.isStringPainted = false
                            cell(progressBar).align(AlignX.RIGHT)
                            label(achievement.progress().toString() + " / " + achievement.nextStep().toString())
                        }
                    }.layout(RowLayout.PARENT_GRID)
                }
            }
            groupRowsRange("Coverage") {
                for (achievement in Util.getCoverageAchievements()) {
                    row {
                        when (achievement.getLevel()) {
                            0 -> icon(TrophyIcons.trophyDefaultIcon)
                            1 -> icon(TrophyIcons.trophyBronzeIcon)
                            2 -> if (!isBright()) {
                                icon(TrophyIcons.trophySilverIcon)
                            } else {
                                icon(TrophyIcons.trophySilverLightIcon)
                            }
                            3 -> icon(TrophyIcons.trophyGoldIcon)
                            else -> {
                                icon(TrophyIcons.trophyPlatinIcon)
                            }
                        }
                        label(achievement.getName()).align(AlignX.LEFT)
                        contextHelp(achievement.getDescription(), achievement.getName())
                        if (achievement.supportsLanguages().contains(Language.Java)) {
                            icon(TrophyIcons.javaIcon)
                        } else {
                            cell()
                        }
                        if (achievement.supportsLanguages().contains(Language.JavaScript)) {
                            icon(TrophyIcons.javaScriptIcon)
                        } else {
                            cell()
                        }
                        if (achievement.getLevel() > 3) {
                            val progressBar = JProgressBar(1, 1)
                            progressBar.value = achievement.progress()
                            progressBar.isStringPainted = false
                            cell(progressBar).align(AlignX.RIGHT)
                            label(achievement.progress().toString())
                        } else {
                            val progressBar = JProgressBar(0, achievement.nextStep())
                            progressBar.value = achievement.progress()
                            progressBar.isStringPainted = false
                            cell(progressBar).align(AlignX.RIGHT)
                            label(achievement.progress().toString() + " / " + achievement.nextStep().toString())
                        }
                    }.layout(RowLayout.PARENT_GRID)
                }
            }
            groupRowsRange("Coverage - Advanced") {
                for (achievement in Util.getAdvancedCoverageAchievements()) {
                    row {
                        when (achievement.getLevel()) {
                            0 -> icon(TrophyIcons.trophyDefaultIcon)
                            1 -> icon(TrophyIcons.trophyBronzeIcon)
                            2 -> if (!isBright()) {
                                icon(TrophyIcons.trophySilverIcon)
                            } else {
                                icon(TrophyIcons.trophySilverLightIcon)
                            }
                            3 -> icon(TrophyIcons.trophyGoldIcon)
                            else -> {
                                icon(TrophyIcons.trophyPlatinIcon)
                            }
                        }
                        label(achievement.getName()).align(AlignX.LEFT)
                        contextHelp(achievement.getDescription(), achievement.getName())
                        if (achievement.supportsLanguages().contains(Language.Java)) {
                            icon(TrophyIcons.javaIcon)
                        } else {
                            cell()
                        }
                        if (achievement.supportsLanguages().contains(Language.JavaScript)) {
                            icon(TrophyIcons.javaScriptIcon)
                        } else {
                            cell()
                        }
                        if (achievement.getLevel() > 3) {
                            val progressBar = JProgressBar(1, 1)
                            progressBar.value = achievement.progress()
                            progressBar.isStringPainted = false
                            cell(progressBar).align(AlignX.RIGHT)
                            label(achievement.progress().toString())
                        } else {
                            val progressBar = JProgressBar(0, achievement.nextStep())
                            progressBar.value = achievement.progress()
                            progressBar.isStringPainted = false
                            cell(progressBar).align(AlignX.RIGHT)
                            label(achievement.progress().toString() + " / " + achievement.nextStep().toString())
                        }
                    }.layout(RowLayout.PARENT_GRID)
                }
            }
            groupRowsRange("Debugging") {
                for (achievement in Util.getDebuggingAchievements()) {
                    row {
                        when (achievement.getLevel()) {
                            0 -> icon(TrophyIcons.trophyDefaultIcon)
                            1 -> icon(TrophyIcons.trophyBronzeIcon)
                            2 -> if (!isBright()) {
                                icon(TrophyIcons.trophySilverIcon)
                            } else {
                                icon(TrophyIcons.trophySilverLightIcon)
                            }
                            3 -> icon(TrophyIcons.trophyGoldIcon)
                            else -> {
                                icon(TrophyIcons.trophyPlatinIcon)
                            }
                        }
                        label(achievement.getName()).align(AlignX.LEFT)
                        contextHelp(achievement.getDescription(), achievement.getName())
                        if (achievement.supportsLanguages().contains(Language.Java)) {
                            icon(TrophyIcons.javaIcon)
                        } else {
                            cell()
                        }
                        if (achievement.supportsLanguages().contains(Language.JavaScript)) {
                            icon(TrophyIcons.javaScriptIcon)
                        } else {
                            cell()
                        }
                        if (achievement.getLevel() > 3) {
                            val progressBar = JProgressBar(1, 1)
                            progressBar.value = achievement.progress()
                            progressBar.isStringPainted = false
                            cell(progressBar).align(AlignX.RIGHT)
                            label(achievement.progress().toString())
                        } else {
                            val progressBar = JProgressBar(0, achievement.nextStep())
                            progressBar.value = achievement.progress()
                            progressBar.isStringPainted = false
                            cell(progressBar).align(AlignX.RIGHT)
                            label(achievement.progress().toString() + " / " + achievement.nextStep().toString())
                        }
                    }.layout(RowLayout.PARENT_GRID)
                }
            }
            groupRowsRange("Test Refactoring") {
                for (achievement in Util.getRefactoringAchievements()) {
                    row {
                        when (achievement.getLevel()) {
                            0 -> icon(TrophyIcons.trophyDefaultIcon)
                            1 -> icon(TrophyIcons.trophyBronzeIcon)
                            2 -> if (!isBright()) {
                                icon(TrophyIcons.trophySilverIcon)
                            } else {
                                icon(TrophyIcons.trophySilverLightIcon)
                            }
                            3 -> icon(TrophyIcons.trophyGoldIcon)
                            else -> {
                                icon(TrophyIcons.trophyPlatinIcon)
                            }
                        }
                        label(achievement.getName()).align(AlignX.LEFT)
                        contextHelp(achievement.getDescription(), achievement.getName())
                        if (achievement.supportsLanguages().contains(Language.Java)) {
                            icon(TrophyIcons.javaIcon)
                        } else {
                            cell()
                        }
                        if (achievement.supportsLanguages().contains(Language.JavaScript)) {
                            icon(TrophyIcons.javaScriptIcon)
                        } else {
                            cell()
                        }
                        if (achievement.getLevel() > 3) {
                            val progressBar = JProgressBar(1, 1)
                            progressBar.value = achievement.progress()
                            progressBar.isStringPainted = false
                            cell(progressBar).align(AlignX.RIGHT)
                            label(achievement.progress().toString())
                        } else {
                            val progressBar = JProgressBar(0, achievement.nextStep())
                            progressBar.value = achievement.progress()
                            progressBar.isStringPainted = false
                            cell(progressBar).align(AlignX.RIGHT)
                            label(achievement.progress().toString() + " / " + achievement.nextStep().toString())
                        }
                    }.layout(RowLayout.PARENT_GRID)
                }
            }
        }

        return panel
    }

    override fun shouldBeAvailable(project: Project) = true
}

class BoldRowRenderer(private val boldRow: Int) : DefaultTableCellRenderer() {
    override fun getTableCellRendererComponent(
        table: JTable, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
    ): Component {
        val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

        // Appliquer une police en gras à la ligne ciblée
        component.font = if (row == boldRow) {
            component.font.deriveFont(Font.BOLD)
        } else {
            component.font.deriveFont(Font.PLAIN)
        }

        return component
    }
}
