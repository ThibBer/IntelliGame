/* * Copyright 2023 IntelliGame contributors
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
 * limitations under the License.*/



package de.uni_passau.fim.se2.intelligame.listeners

import com.intellij.coverage.BaseCoverageSuite
import com.intellij.coverage.CoverageDataManager
import com.intellij.coverage.CoverageSuite
import com.intellij.coverage.CoverageSuiteListener
import com.intellij.coverage.CoverageSuitesBundle
import de.uni_passau.fim.se2.intelligame.achievements.*
import de.uni_passau.fim.se2.intelligame.util.CoverageInfo
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import de.uni_passau.fim.se2.intelligame.services.GamificationService
import de.uni_passau.fim.se2.intelligame.util.Util
import java.lang.reflect.Field
object CoverageListener : CoverageSuiteListener {
    lateinit var project: Project
    private lateinit var testRunName: String

    override fun coverageGathered(suite: CoverageSuite) {
        project = suite.project

        testRunName = (suite as BaseCoverageSuite).configuration!!.name

        if(!Util.isTestExcluded(testRunName.split(".").first())){
            RunWithCoverageAchievement.triggerAchievement(project)
        }

        super.coverageGathered(suite)
    }

    override fun beforeSuiteChosen() = Unit

    override fun afterSuiteChosen() {
        val dataManager = CoverageDataManager.getInstance(project)
        if (ApplicationManager.getApplication().isUnitTestMode) {
            return
        }

        val suitesBundle: CoverageSuitesBundle = dataManager.currentSuitesBundle ?: return
        val annotator = suitesBundle.coverageEngine.getCoverageAnnotator(project)

        val modalTask: Task.Modal = object : Task.Modal(project, "Modal Cancelable Task", false) {
            override fun run(indicator: ProgressIndicator) {
                if (annotator::class.simpleName == "JavaCoverageAnnotator") {
                    javaCoverage()
                }
            }

            fun javaCoverage() {
                // Check for class coverage information
                val classCoverageInfosField: Field = annotator.javaClass.getDeclaredField("myClassCoverageInfos")
                classCoverageInfosField.isAccessible = true
                val classCoverageInfosValue: Map<Any, Any> = classCoverageInfosField.get(annotator) as Map<Any, Any>

                val gamificationService = project.service<GamificationService>()

                val runClassName = testRunName.split(".").first().replace("Test", "")

                for ((key, value) in classCoverageInfosValue.filter { (it.key as String).contains(runClassName) && !Util.isTestExcluded(it.key as String) }) {
                    val coverageInfo = extractCoverageInfos(value)

                    GetXLineCoverageInClassesWithYLinesAchievement.triggerAchievement(coverageInfo, key as String, project)
                    GetXBranchCoverageInClassesWithYBranchesAchievement.triggerAchievement(coverageInfo, key, project)
                    GetXMethodCoverageInClassesWithYMethodsAchievement.triggerAchievement(coverageInfo, key, project)
                    CoverXLinesAchievement.triggerAchievement(coverageInfo, project)
                    CoverXMethodsAchievement.triggerAchievement(coverageInfo, project)
                    CoverXClassesAchievement.triggerAchievement(coverageInfo, project)
                    CoverXBranchesAchievement.triggerAchievement(coverageInfo, project)

                    gamificationService.updateCoverage(coverageInfo, key, testRunName, project)
                }

                val extensionCoverageField: Field = annotator.javaClass.getDeclaredField("myDirCoverageInfos")
                extensionCoverageField.isAccessible = true

                val extensionCoverageInfosValue: Map<Any, Any> = extensionCoverageField.get(annotator) as Map<Any, Any>

                if (extensionCoverageInfosValue.isEmpty()) {
                    ApplicationManager.getApplication().invokeLater(fun() {
                        ProgressManager.getInstance().run(this)
                    })
                }
            }
        }

        ApplicationManager.getApplication().invokeLater(fun() {
            ProgressManager.getInstance().run(modalTask)
        })
    }

    private fun extractCoverageInfos(coverageInfo: Any): CoverageInfo {
        val coveredLineCount = coverageInfo.javaClass.getMethod("getCoveredLineCount").invoke(coverageInfo) as Int
        val totalLineCount = getFieldAsInt(coverageInfo, "totalLineCount")
        val totalClassCount = getFieldAsInt(coverageInfo, "totalClassCount")
        val coveredClassCount = getFieldAsInt(coverageInfo, "coveredClassCount")
        val totalMethodCount = getFieldAsInt(coverageInfo, "totalMethodCount")
        val coveredMethodCount = getFieldAsInt(coverageInfo, "coveredMethodCount")
        val coveredBranchCount = getFieldAsInt(coverageInfo, "coveredBranchCount")
        val totalBranchCount = getFieldAsInt(coverageInfo, "totalBranchCount")
        return CoverageInfo(
            totalClassCount,
            coveredClassCount,
            totalMethodCount,
            coveredMethodCount,
            totalLineCount,
            coveredLineCount,
            totalBranchCount,
            coveredBranchCount
        )
    }

    private fun findUnderlyingField(clazz: Class<*>, fieldName: String): Field? {
        var current = clazz

        do {
            try {
                return current.getDeclaredField(fieldName)
            } catch (_: Exception) {}
        } while (current.superclass.also { current = it } != null)

        return null
    }

    private fun getFieldAsInt(coverageInfo: Any, fieldName: String): Int {
        val field: Field? = findUnderlyingField(coverageInfo.javaClass, fieldName)
        if (field == null) {
            return 0
        }

        return field.get(coverageInfo) as Int

    }
}
