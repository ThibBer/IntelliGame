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

import de.uni_passau.fim.se2.intelligame.util.CoverageInfo
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import de.uni_passau.fim.se2.intelligame.util.GameMode

object CoverXBranchesAchievement : Achievement() {
    fun triggerAchievement(coverageInfo: CoverageInfo, project: Project?) {
        if(coverageInfo.coveredBranchCount <= 0){
            return
        }

        var progress = progress()
        progress += coverageInfo.coveredBranchCount
        handleProgress(progress, project)
    }

    override fun progress(): Int {
        val properties = PropertiesComponent.getInstance()
        return properties.getInt(getPropertyKey(), 0)
    }

    override fun updateProgress(progress: Int) {
        val properties = PropertiesComponent.getInstance()
        properties.setValue(getPropertyKey(), progress, 0)
    }

    override fun getDescription(): String {
        return "Cover X branches with your tests. Attention: for this achievement the tracing option of " +
                "the IntelliJ Runner must be enabled."
    }

    override fun getName(): String {
        return "Check your branches"
    }

    override fun getStepLevelMatrix(): LinkedHashMap<Int, Int> {
        return linkedMapOf(0 to 2, 1 to 25, 2 to 50, 3 to 100)
    }

    override fun supportedGameModes(): List<GameMode> {
        return listOf(GameMode.ACHIEVEMENTS, GameMode.LEADERBOARD)
    }
}