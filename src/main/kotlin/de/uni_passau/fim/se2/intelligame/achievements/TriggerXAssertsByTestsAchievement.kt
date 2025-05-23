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

import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.ide.DataManager
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.FileIndexFacade
import com.intellij.psi.search.ProjectScopeImpl
import de.uni_passau.fim.se2.intelligame.util.GameMode
import de.uni_passau.fim.se2.intelligame.util.Util
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities

object TriggerXAssertsByTestsAchievement : SMTRunnerEventsListener, Achievement() {
    private var project: Project? = null

    override fun onTestingStarted(testsRoot: SMTestProxy.SMRootTestProxy) {
        project = Util.getProject(testsRoot.locationUrl)
    }

    override fun onTestingFinished(testsRoot: SMTestProxy.SMRootTestProxy) = Unit

    override fun onTestsCountInSuite(count: Int) = Unit

    override fun onTestStarted(test: SMTestProxy) {
        if (project == null) project = Util.getProject(test.locationUrl)
    }

    override fun onTestFinished(test: SMTestProxy) {
        if(Util.isTestExcluded(test.locationUrl) || !test.isPassed){
            return
        }

        val project = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(10, TimeUnit.SECONDS)!!.getData(PlatformDataKeys.PROJECT)!!
        SwingUtilities.invokeLater {
            val file = test.getLocation(project, ProjectScopeImpl(project, FileIndexFacade.getInstance(project)))!!.virtualFile

            if (file!!.path.endsWith("Test.java")) {
                val testText =
                    test.getLocation(project, ProjectScopeImpl(project, FileIndexFacade.getInstance(project)))!!
                        .psiElement.text

                val count = "assert".toRegex().findAll(testText).count()

                var progress = progress()
                progress += count
                handleProgress(progress, project)
            }

        }
    }

    override fun onTestFailed(test: SMTestProxy) = Unit

    override fun onTestIgnored(test: SMTestProxy) = Unit

    override fun onSuiteFinished(suite: SMTestProxy) = Unit

    override fun onSuiteStarted(suite: SMTestProxy) = Unit

    override fun onCustomProgressTestsCategory(categoryName: String?, testCount: Int) = Unit

    override fun onCustomProgressTestStarted() = Unit

    override fun onCustomProgressTestFailed() = Unit

    override fun onCustomProgressTestFinished() = Unit

    override fun onSuiteTreeNodeAdded(testProxy: SMTestProxy?) = Unit

    override fun onSuiteTreeStarted(suite: SMTestProxy?) = Unit

    override fun getPropertyKey(): String {
        return "AssertTriggeredByTest"
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
        return "Asserts have to be successfully passed by tests"
    }

    override fun getName(): String {
        return "Assert and Tested"
    }

    override fun getStepLevelMatrix(): LinkedHashMap<Int, Int> {
        return linkedMapOf(0 to 3, 1 to 10, 2 to 100, 3 to 1000)
    }

    override fun supportedGameModes(): List<GameMode> {
        return listOf(GameMode.ACHIEVEMENTS, GameMode.LEADERBOARD)
    }
}