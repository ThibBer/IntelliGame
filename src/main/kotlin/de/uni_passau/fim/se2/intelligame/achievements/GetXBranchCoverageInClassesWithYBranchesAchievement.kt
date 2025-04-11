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

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import de.uni_passau.fim.se2.intelligame.util.CoverageInfo
import de.uni_passau.fim.se2.intelligame.util.GameMode

object GetXBranchCoverageInClassesWithYBranchesAchievement : Achievement() {
    fun triggerAchievement(coverageInfo: CoverageInfo, className: String, project: Project?) {
        if (coverageInfo.totalBranchCount >= requiredTotalBranches()
            && !getClassesWhichFulfillRequirements().split(",").contains(className)
        ) {
            val achievedCoverage = coverageInfo.coveredBranchCount.toDouble() / coverageInfo.totalBranchCount
            if (achievedCoverage >= requiredCoverage()) {
                var classesWhichFulfillRequirements = getClassesWhichFulfillRequirements()
                if (classesWhichFulfillRequirements == "") {
                    classesWhichFulfillRequirements = className
                } else {
                    classesWhichFulfillRequirements += ",$className"
                }
                updateClassesWhichFulfillRequirements(classesWhichFulfillRequirements)
                if (progress() == nextStep()) {
                    showAchievementNotification("Congratulations! You unlocked level " +
                            (getLevel() + 1) + " of the 'Class Reviewer - Branches' Achievement", project)
                    updateClassesWhichFulfillRequirements("")
                    increaseLevel()
                }
            } else if (achievedCoverage >= requiredCoverage() - 0.02) {
                showAchievementNotification(
                    "Hey you are about to fulfill a requirement for an Achievement progress! Only " + "%.2f".format(
                        (requiredCoverage() - achievedCoverage) * 100
                    ) + "% Branch-coverage missing in the class " + className + ". Keep going!", project
                )
            }
        }
        refreshWindow()
    }

    override fun progress(): Int {
        val properties = PropertiesComponent.getInstance()
        val value = properties.getValue(getPropertyKey(), "")
        return if (value == "") {
            0
        } else {
            value.split(",").size
        }
    }

    override fun updateProgress(progress: Int) = Unit

    private fun updateClassesWhichFulfillRequirements(classesWhichFulfillRequirements: String) {
        val properties = PropertiesComponent.getInstance()
        properties.setValue(getPropertyKey(), classesWhichFulfillRequirements, "")
    }

    private fun getClassesWhichFulfillRequirements(): String {
        val properties = PropertiesComponent.getInstance()
        return properties.getValue(getPropertyKey(), "")
    }

    override fun getLevel(): Int {
        val properties = PropertiesComponent.getInstance()
        return properties.getInt(getLevelPropertyKey(), 0)
    }

    private fun increaseLevel() {
        val properties = PropertiesComponent.getInstance()
        properties.setValue(getLevelPropertyKey(), (getLevel() + 1), 0)
    }

    override fun getDescription(): String {
        return "Cover " + nextStep() + " classes which have at least "+
                requiredTotalBranches() + " branches by at least " +
                requiredCoverage() * 100 + "%. Attention: for this achievement the tracing option of " +
                "the IntelliJ Runner must be enabled."
    }

    override fun getName(): String {
        return "Class Reviewer - Branches"
    }

    override fun getStepLevelMatrix(): LinkedHashMap<Int, Int> {
        return linkedMapOf(0 to 5, 1 to 20, 2 to 75, 3 to 250)
    }

    private fun requiredCoverage(): Double {
        val level = getLevel()
        if (level <= 1) {
            return 0.75
        }

        if (level <= 2) {
            return 0.80
        }

        if (level <= 3) {
            return 0.85
        }

        return 0.90
    }

    private fun requiredTotalBranches(): Int {
        val level = getLevel()
        if (level <= 1) {
            return 15
        }

        if (level <= 2) {
            return 50
        }

        if (level <= 3) {
            return 250
        }

        return 500
    }

    override fun supportedGameModes(): List<GameMode> {
        return listOf(GameMode.ACHIEVEMENTS)
    }
}