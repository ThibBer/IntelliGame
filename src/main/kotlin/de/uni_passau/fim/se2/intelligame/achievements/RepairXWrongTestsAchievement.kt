package de.uni_passau.fim.se2.intelligame.achievements

import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener
import com.intellij.execution.testframework.sm.runner.SMTestProxy
import com.intellij.execution.testframework.sm.runner.states.TestStateInfo
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.charset.Charset

object RepairXWrongTestsAchievement : SMTRunnerEventsListener, Achievement() {
    private var testsUnderObservation = hashMapOf<String, String>()
    private var classesUnderObservation = hashMapOf<String, String>()
    private var project: Project? = null

    override fun onTestingStarted(testsRoot: SMTestProxy.SMRootTestProxy) {
        val projects = ProjectManager.getInstance().openProjects
        for (p in projects) {
            if (p.basePath?.let { testsRoot.locationUrl?.contains(it) } == true) {
                project = p
            }
        }
    }

    override fun onTestingFinished(testsRoot: SMTestProxy.SMRootTestProxy) = Unit

    override fun onTestsCountInSuite(count: Int) = Unit

    override fun onTestStarted(test: SMTestProxy) = Unit

    override fun onTestFinished(test: SMTestProxy) {
        val key = test.locationUrl
        val fileUrl = (test.locationUrl?.removeRange(test.locationUrl!!.lastIndexOf("/"), test.locationUrl!!.length)
            ?.removePrefix("java:test://")
            ?.replace(".", "/")
            ?: "")
        val pathToTest =
            project?.basePath + "/src/test/java/" + fileUrl + ".java"
        val pathToCode =
            project?.basePath + "/src/main/java/" + fileUrl.dropLast(4) + ".java"
        val testFile = File(pathToTest)
        val codeFile = File(pathToCode)
        if (key != null && testFile.exists() && codeFile.exists()) {
            val testFileContent = FileUtils.readFileToString(testFile, Charset.defaultCharset())
                .replace(System.getProperty("line.separator"), "")
            val codeFileContent = FileUtils.readFileToString(codeFile, Charset.defaultCharset())
                .replace(System.getProperty("line.separator"), "")
            if (test.magnitudeInfo == TestStateInfo.Magnitude.FAILED_INDEX) {
                if (!testsUnderObservation.containsKey(key) && !classesUnderObservation.containsKey(key)) {
                    testsUnderObservation[key] = testFileContent
                    classesUnderObservation[key] = codeFileContent
                }
            } else if (test.magnitudeInfo == TestStateInfo.Magnitude.PASSED_INDEX) {
                if (testsUnderObservation.containsKey(key) && classesUnderObservation.containsKey(key) &&
                    testsUnderObservation[key] != testFileContent && classesUnderObservation[key] == codeFileContent
                ) {
                    var progress = progress()
                    progress += 1
                    handleProgress(progress, project)
                    testsUnderObservation.remove(key)
                    classesUnderObservation.remove(key)
                } else {
                    testsUnderObservation.remove(key)
                    classesUnderObservation.remove(key)
                }
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

    override fun progress(): Int {
        val properties = PropertiesComponent.getInstance()
        return properties.getInt("repairedXTests", 0)
    }

    override fun updateProgress(progress: Int) {
        val properties = PropertiesComponent.getInstance()
        properties.setValue("repairedXTests", progress, 0)
    }

    override fun getDescription(): String {
        return "Repair wrong tests"
    }

    override fun getName(): String {
        return "Test Fixer"
    }

    override fun getStepLevelMatrix(): LinkedHashMap<Int, Int> {
        return linkedMapOf(0 to 3, 1 to 10, 2 to 100, 3 to 1000)
    }

    override fun supportsLanguages(): List<Language> {
        return listOf(Language.Java)
    }
}