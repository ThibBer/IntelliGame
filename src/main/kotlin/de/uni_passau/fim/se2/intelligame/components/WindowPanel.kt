package de.uni_passau.fim.se2.intelligame.components

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBTabbedPane
import de.uni_passau.fim.se2.intelligame.services.GamificationService
import de.uni_passau.fim.se2.intelligame.util.GameMode
import javax.swing.JComponent

class WindowPanel(val project: Project) : JBTabbedPane() {
    private val properties = PropertiesComponent.getInstance()
    private val gamificationService = project.service<GamificationService>()

    fun create(): JComponent {
        if(properties.getValue("gamification-api-key").isNullOrBlank()) {
            return ApiSettingsUI.create(project)
        }

        val tabbedPane = JBTabbedPane()

        val leaderboard = LeaderboardUI.create(project)
        tabbedPane.addTab("Leaderboard", leaderboard)

        val achievements = AchievementsUI.create(project)
        tabbedPane.addTab("Achievements", achievements)

        tabbedPane.addChangeListener {
            val tabIndex = tabbedPane.selectedIndex
            properties.setValue("gamification-active-tabs", tabIndex.toString())

            if(tabIndex == GameMode.LEADERBOARD.ordinal || tabIndex == GameMode.ACHIEVEMENTS.ordinal) {
                gamificationService.setGameMode(GameMode.entries[tabIndex])
            }
        }

        val settings = SettingsUI.create(project)
        tabbedPane.addTab("Settings", settings)

        tabbedPane.selectedIndex = properties.getValue("gamification-active-tabs", "0").toInt()

        return tabbedPane
    }
}