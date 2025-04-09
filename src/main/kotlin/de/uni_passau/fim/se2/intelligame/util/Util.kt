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
import com.intellij.openapi.project.ProjectManager
import de.uni_passau.fim.se2.intelligame.MyBundle
import de.uni_passau.fim.se2.intelligame.achievements.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.random.Random

val adjectives = listOf("Rapide", "Mystique", "Fou", "Brillant", "Sombre", "Éclair")
val nouns = listOf("Tigre", "Phoenix", "Dragon", "Loup", "Hérisson", "Hibou", "Chat", "Renard")

object Util {
    fun getAchievements(): List<Achievement> {
        return listOf(
            AddTestsAchievement,
            CoverXBranchesAchievement,
            CoverXClassesAchievement,
            CoverXLinesAchievement,
            CoverXMethodsAchievement,
            FindXBugsAchievement,
            GetXBranchCoverageInClassesWithYBranchesAchievement,
            GetXLineCoverageInClassesWithYLinesAchievement,
            GetXMethodCoverageInClassesWithYMethodsAchievement,
            RefactorAddXAssertionsAchievement,
            RefactorCodeAchievement,
            RefactorExtractXMethodsAchievement,
            RefactorInlineXMethodsAchievement,
            RefactorXTestNamesAchievement,
            RepairXWrongTestsAchievement,
            RunWithCoverageAchievement,
            RunXTestSuitesAchievement,
            RunXTestSuitesWithXTestsAchievement,
            TriggerXAssertsByTestsAchievement
        )
    }

    fun getRefactoringAchievements(): List<Achievement> {
        return listOf(
            RefactorCodeAchievement,
            RefactorXTestNamesAchievement,
            RefactorExtractXMethodsAchievement,
            RefactorInlineXMethodsAchievement,
            RefactorAddXAssertionsAchievement
        )
    }

    fun getCoverageAchievements(): List<Achievement> {
        return listOf(
            RunWithCoverageAchievement,
            CoverXLinesAchievement,
            CoverXMethodsAchievement,
            CoverXClassesAchievement,
            CoverXBranchesAchievement
        )
    }

    fun getAdvancedCoverageAchievements(): List<Achievement> {
        return listOf(
            GetXLineCoverageInClassesWithYLinesAchievement,
            GetXMethodCoverageInClassesWithYMethodsAchievement,
            GetXBranchCoverageInClassesWithYBranchesAchievement
        )
    }

    fun getTestsAchievement(): List<Achievement> {
        return listOf(
            RunXTestSuitesAchievement,
            RunXTestSuitesWithXTestsAchievement,
            TriggerXAssertsByTestsAchievement,
            FindXBugsAchievement,
            RepairXWrongTestsAchievement,
            AddTestsAchievement
        )
    }

    fun getProject(locationUrl: String?): Project? {
        val projects = ProjectManager.getInstance().openProjects
        var project: Project? = null

        if (projects.size == 1) {
            project = projects[0]
            return project
        }

        for (p in projects) {
            if (p.basePath?.let { locationUrl?.contains(it) } == true) {
                project = p
            }
        }

        return project
    }

    fun generatePseudo(): String {
        val adj = adjectives.random()
        val noun = nouns.random()
        val number = Random.nextInt(1000)

        return "$adj$noun$number"
    }

    fun getEvaluationDirectoryPath(project: Project): String{
        return project.basePath + File.separator + ".evaluation"
    }

    fun getEvaluationFilePath(project: Project, filename: String): String{
        return getEvaluationDirectoryPath(project) + File.separator + filename
    }

    fun isTestExcluded(testName: String?): Boolean{
        if(testName == null){
            return false
        }

        return MyBundle.getMessage("excludedTestClasses")
            .split(",")
            .map{ it.trim().replace("/", ".").replace("\\", ".") }
            .any { testName.contains(it, true) }
    }

    fun zipFolder(sourceDirPath: String, zipFilePath: String) {
        val sourceDir = File(sourceDirPath)

        ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFilePath))).use { zipOut ->
            zipDirectoryRecursive(sourceDir, sourceDir.name, zipOut)
        }
    }

    private fun zipDirectoryRecursive(fileToZip: File, fileName: String, zipOut: ZipOutputStream) {
        if (fileToZip.isHidden) {
            return
        }

        if (fileToZip.isDirectory) {
            if (fileName.endsWith("/").not()) {
                zipOut.putNextEntry(ZipEntry("$fileName/"))
                zipOut.closeEntry()
            }
            fileToZip.listFiles()?.forEach { childFile ->
                zipDirectoryRecursive(childFile, "$fileName/${childFile.name}", zipOut)
            }

            return
        }

        FileInputStream(fileToZip).use { fis ->
            val zipEntry = ZipEntry(fileName)
            zipOut.putNextEntry(zipEntry)
            fis.copyTo(zipOut)
        }
    }
}