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

package de.uni_passau.fim.se2.intelligame.util

import com.intellij.openapi.project.Project
import java.sql.Timestamp
import java.util.stream.Collectors

object CSVReportGenerator {
    private const val FILE_NAME = "AchievementsReport.csv"

    fun generateCSVReport(project: Project?) {
        if (project == null) {
            return
        }

        val achievements = Util.getAchievements()

        val achievementNamesLevel = achievements.stream().map { it.getName() + "Level" }.collect(Collectors.toList())
        val achievementNamesProgress = achievements.stream().map { it.getName() + "Progress" }.collect(Collectors.toList())

        val header = mutableListOf<String>()
        header += listOf("Timestamp")
        header += achievementNamesLevel
        header += achievementNamesProgress

        try {
            val csvFile = CSVFile(header)

            val timestamp = Timestamp(System.currentTimeMillis()).toString()
            val row = mutableListOf<String>()

            row += listOf(timestamp)
            row += achievements.stream().map { it.getLevel().toString() }.collect(Collectors.toList())
            row += achievements.stream().map { it.progress().toString() }.collect(Collectors.toList())

            csvFile.appendLine(row)

            val path = Util.getEvaluationFilePath(project, FILE_NAME)
            csvFile.save(path)
        } catch (_: Exception) {

        }
    }
}