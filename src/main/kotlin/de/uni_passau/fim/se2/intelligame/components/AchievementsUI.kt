package de.uni_passau.fim.se2.intelligame.components

import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor.isBright
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import de.uni_passau.fim.se2.intelligame.util.AchievementCategoryType
import de.uni_passau.fim.se2.intelligame.util.Util
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.JProgressBar

class AchievementsUI {
    companion object {
        private val categories: List<AchievementCategory> = listOf(
            AchievementCategory(AchievementCategoryType.TESTING, Util.getTestsAchievement()),
            AchievementCategory(AchievementCategoryType.COVERAGE, Util.getCoverageAchievements()),
            AchievementCategory(AchievementCategoryType.COVERAGE_ADVANCED, Util.getAdvancedCoverageAchievements()),
            AchievementCategory(AchievementCategoryType.DEBUGGING, Util.getDebuggingAchievements()),
            AchievementCategory(AchievementCategoryType.TEST_REFACTORING, Util.getRefactoringAchievements()),
        )

        private val trophies: List<Pair<Icon, String>> = listOf(
            Pair(TrophyIcons.trophyDefaultIcon, "None"),
            Pair(TrophyIcons.trophyBronzeIcon, "Bronze"),
            Pair(if (isBright()) TrophyIcons.trophySilverLightIcon else TrophyIcons.trophySilverIcon, "Silver"),
            Pair(TrophyIcons.trophyGoldIcon, "Gold"),
            Pair(TrophyIcons.trophyPlatinIcon, "Platinum"),
        )

        fun create(project: Project): JPanel {
            val panel = JPanel(BorderLayout())

            val achievement = achievementList()

            val scrollPane = JBScrollPane(achievement)
            scrollPane.setBorder(BorderFactory.createEmptyBorder())
            scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT)

            panel.add(scrollPane, BorderLayout.CENTER)

            val websocketPanel = WebsocketUI.create(project)
            panel.add(websocketPanel, BorderLayout.SOUTH)

            return panel
        }

        private fun achievementList(): JPanel {
            val panel = panel {
                row("Available trophies") {
                    label("").resizableColumn()
                    for(trophy in trophies){
                        panel{
                            row { icon(trophy.first).align(Align.CENTER) }
                            row { label(trophy.second).align(Align.CENTER) }
                        }
                    }
                    label("").resizableColumn()
                }

                for(category in categories) {
                    groupRowsRange(category.label.toString()) {
                        for (achievement in category.achievements) {
                            row {
                                icon(getTrophyIcon(achievement.getLevel()))
                                label(achievement.getName()).align(AlignX.LEFT)
                                contextHelp(achievement.getDescription(), achievement.getName())

                                val achievementLevelGreaterThan3 = achievement.getLevel() > 3
                                val progressBar = if (achievementLevelGreaterThan3) JProgressBar(1, 1) else JProgressBar(0, achievement.nextStep())
                                progressBar.value = achievement.progress()
                                progressBar.isStringPainted = false
                                cell(progressBar).align(AlignX.RIGHT)

                                var label = achievement.progress().toString()
                                if (!achievementLevelGreaterThan3) {
                                    label += " / " + achievement.nextStep().toString()
                                }

                                label(label)
                            }.resizableRow()
                        }
                    }
                }
            }

            return panel
        }

        private fun getTrophyIcon(achievementLevel: Int): Icon {
            return when (achievementLevel) {
                0 -> TrophyIcons.trophyDefaultIcon
                1 -> TrophyIcons.trophyBronzeIcon
                2 -> if (isBright()) { TrophyIcons.trophySilverLightIcon } else { TrophyIcons.trophySilverIcon }
                3 -> TrophyIcons.trophyGoldIcon
                else -> {
                    TrophyIcons.trophyPlatinIcon
                }
            }
        }
    }
}