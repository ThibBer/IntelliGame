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
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import de.uni_passau.fim.se2.intelligame.util.GameMode
import de.uni_passau.fim.se2.intelligame.util.Util

object RunXTestSuitesWithXTestsAchievement : SMTRunnerEventsListener, Achievement() {
    private var project: Project? = null

    override fun onTestingStarted(testsRoot: SMTestProxy.SMRootTestProxy) {
        project = Util.getProject(testsRoot.locationUrl)
    }

    override fun onTestingFinished(testsRoot: SMTestProxy.SMRootTestProxy) {
    }

    fun triggerAchievement(tests: Int) {
        if (tests >= requiredTestsInSuite()) {
            var progress = progress()
            progress += 1
            handleProgress(progress, project)
        }
    }

    override fun onTestsCountInSuite(count: Int) = Unit

    override fun onTestStarted(test: SMTestProxy) {
        if (project == null) {
            project = Util.getProject(test.locationUrl)
        }
    }

    override fun onTestFinished(test: SMTestProxy) = Unit

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

    override fun getLevel(): Int {
        val properties = PropertiesComponent.getInstance()
        return properties.getInt(getLevelPropertyKey(), 0)
    }

    private fun increaseLevel() {
        val properties = PropertiesComponent.getInstance()
        properties.setValue(getLevelPropertyKey(), (getLevel() + 1), 0)
    }

    override fun progress(): Int {
        val properties = PropertiesComponent.getInstance()
        return properties.getInt(getPropertyKey(), 0)
    }

    override fun updateProgress(progress: Int) {
        val properties = PropertiesComponent.getInstance()
        properties.setValue(getPropertyKey(), progress, 0)
        if (progress >= nextStep()) increaseLevel()
    }

    override fun getDescription(): String {
        return "Run " + nextStep() + " times test suites containing at least " + requiredTestsInSuite() + " tests"
    }

    override fun getName(): String {
        return "The Tester - Advanced"
    }

    override fun getStepLevelMatrix(): LinkedHashMap<Int, Int> {
        return linkedMapOf(0 to 10, 1 to 50, 2 to 100, 3 to 250)
    }

    private fun requiredTestsInSuite(): Int {
        val level = getLevel()
        if (level <= 1) {
            return 10
        }

        if (level <= 2) {
            return 20
        }

        if (level <= 3) {
            return 30
        }

        return 40
    }

    override fun supportedGameModes(): List<GameMode> {
        return listOf(GameMode.ACHIEVEMENTS)
    }
}