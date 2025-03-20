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

package de.uni_passau.fim.se2.intelligame.achievements

import com.intellij.ide.DataManager
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import de.uni_passau.fim.se2.intelligame.components.GamificationToolWindow
import de.uni_passau.fim.se2.intelligame.services.GamificationService
import de.uni_passau.fim.se2.intelligame.util.CSVReportGenerator
import de.uni_passau.fim.se2.intelligame.util.GameMode
import de.uni_passau.fim.se2.intelligame.util.Logger
import java.util.concurrent.TimeUnit

abstract class Achievement {
    enum class Language {
        Java, JavaScript
    }

    companion object {
        fun refreshWindow() {
            GamificationToolWindow.refresh()
        }
    }

    // absolute number of calling the action of the achievement
    abstract fun progress(): Int

    // update progress
    abstract fun updateProgress(progress: Int)

    abstract fun getDescription(): String

    abstract fun getName(): String

    /**
     * The key is the level (0-4) and the value is the required progress to achieve this level
     */
    abstract fun getStepLevelMatrix(): LinkedHashMap<Int, Int>

    open fun getLevel(): Int {
        val stepLevelMatrix = getStepLevelMatrix()
        val progress = progress()
        for ((key, value) in stepLevelMatrix) {
            if (progress < value) {
                return key
            }
        }
        return 4
    }

    /**
     * Get the next step with the help of the stepLevelMatrix.
     * Next step = which progress you need to get to the next level.
     */
    open fun nextStep(): Int {
        val stepLevelMatrix = getStepLevelMatrix()
        val progress = progress()
        for ((_, value) in stepLevelMatrix) {
            if (progress < value) {
                return value
            }
        }
        return stepLevelMatrix.getValue(stepLevelMatrix.size - 1)
    }

    /**
     * Shows the balloon with the given message.
     */
    fun showAchievementNotification(message: String, project: Project?) {
        if(project != null && project.service<GamificationService>().getGameMode() != GameMode.ACHIEVEMENTS){
            return
        }

        val group = NotificationGroupManager.getInstance().getNotificationGroup("Gamification")
        val notification = group.createNotification(
                message,
                NotificationType.INFORMATION
            )
            .addAction(
                NotificationAction.createSimple("Show more information") {
                    val myProject = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(10, TimeUnit.SECONDS)!!.getData(PlatformDataKeys.PROJECT)
                    val toolWindow = ToolWindowManager.getInstance(myProject!!).getToolWindow("Gamification")!!
                    refreshWindow()
                    toolWindow.show()
                }
            )

        notification.notify(null)

        Logger.logStatus(message, Logger.Kind.Notification, project)
    }

    /**
     * Get the current progress group
     * Groups are Divided:
     * 0: 0 - 24,9%
     * 1: 25% - 49,9%
     * 2: 50% - 74,9%
     * 3: 75% - 100%
     */
    private fun getProgressGroup(): Pair<Int, String> {
        val progressInPercent = (progress().toFloat() / nextStep())
        val reachedPercentage = "%.2f".format((progressInPercent * 100))
        if (progressInPercent < 0.25) {
            return Pair(0, reachedPercentage)
        }

        if (progressInPercent < 0.5) {
            return Pair(1, reachedPercentage)
        }

        if (progressInPercent < 0.75) {
            return Pair(2, reachedPercentage)
        }

        return Pair(3, reachedPercentage)
    }

    protected fun handleProgress(progress: Int, project: Project?) {
        if(project == null){
            return
        }

        val gamificationService = project.service<GamificationService>()

        val pointsToAdd = progress - progress()
        gamificationService.addPoints(pointsToAdd, this::class)

        println("GameMode : " + gamificationService.getGameMode())

        if(gamificationService.getGameMode() == GameMode.ACHIEVEMENTS){
            if (progress >= nextStep()) {
                updateProgress(progress)
                showAchievementNotification(
                    "Congratulations! You unlocked level ${getLevel()} of the '${getName()}' achievement!", project
                )
            } else {
                val progressGroupBeforeUpdate = getProgressGroup()
                updateProgress(progress)

                val progressGroupAfterUpdate = getProgressGroup()
                if (progressGroupAfterUpdate.first > progressGroupBeforeUpdate.first) {
                    showAchievementNotification(
                        "You are making progress on an achievement! You have already reached " +
                                progressGroupAfterUpdate.second + "% of the next level of the '" +
                                getName() + "' achievement!",
                        project
                    )
                }
            }

            refreshWindow()
            CSVReportGenerator.generateCSVReport(project)
        }

    }

    abstract fun supportsLanguages(): List<Language>

    open fun getPropertyKey(): String{
        return this::class.simpleName!!
    }

    open fun getLevelPropertyKey(): String{
        return getPropertyKey() + "Level"
    }
}