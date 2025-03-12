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
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.content.ContentFactory
import de.uni_passau.fim.se2.intelligame.services.GamificationService
import de.uni_passau.fim.se2.intelligame.util.GameMode
import java.util.concurrent.TimeUnit
import javax.swing.JComponent
import javax.swing.SwingUtilities


class GamificationToolWindow : ToolWindowFactory {
    private val contentFactory = ContentFactory.getInstance()

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = createPanel(project)
        val content = contentFactory.createContent(panel, null, false)

        toolWindow.setIcon(TrophyIcons.trophyToolWindowIcon)
        toolWindow.contentManager.addContent(content)
    }

    companion object {
        private val properties = PropertiesComponent.getInstance()

        fun createPanel(project: Project): JComponent {
            val gamificationService = project.service<GamificationService>()

            if(!properties.isValueSet("gamification-api-key")){
                return ApiSettingsUI.create(project)
            }

            val tabbedPane = JBTabbedPane()

            val leaderboard = LeaderboardUI.create(project)
            tabbedPane.addTab("Leaderboard", leaderboard)

            val badges = AchievementsUI.create(project)
            tabbedPane.addTab("Badges", badges)

            tabbedPane.addChangeListener {
                gamificationService.setGameMode(GameMode.entries[tabbedPane.selectedIndex])
            }

            tabbedPane.selectedIndex = gamificationService.getGameMode().ordinal

            return tabbedPane
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

    override fun shouldBeAvailable(project: Project) = true
}