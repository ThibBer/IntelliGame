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

package de.uni_passau.fim.se2.intelligame.services

import com.intellij.coverage.CoverageDataManager
import com.intellij.execution.ExecutionManager
import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.xdebugger.XDebuggerManager
import com.intellij.xdebugger.breakpoints.XBreakpointListener
import de.uni_passau.fim.se2.intelligame.achievements.*
import de.uni_passau.fim.se2.intelligame.listeners.BulkFileListenerImpl
import de.uni_passau.fim.se2.intelligame.listeners.ConsoleListener
import de.uni_passau.fim.se2.intelligame.listeners.CoverageListener

@Service(Service.Level.PROJECT)
class ProjectService(val project: Project) : Disposable {
    init {
        println("Project service")

        project.messageBus.connect(this).subscribe(SMTRunnerEventsListener.TEST_STATUS, TriggerXAssertsByTestsAchievement)
        project.messageBus.connect(this).subscribe(XDebuggerManager.TOPIC, RunXDebuggerModeAchievement)
        project.messageBus.connect(this).subscribe(SMTRunnerEventsListener.TEST_STATUS, RunXTestsAchievement)
        project.messageBus.connect(this).subscribe(SMTRunnerEventsListener.TEST_STATUS, RunXTestSuitesAchievement)
        project.messageBus.connect(this).subscribe(SMTRunnerEventsListener.TEST_STATUS, RunXTestSuitesWithXTestsAchievement)
        project.messageBus.connect(this).subscribe(XBreakpointListener.TOPIC, SetXBreakpointsAchievement)
        project.messageBus.connect(this).subscribe(XBreakpointListener.TOPIC, SetXConditionalBreakpointsAchievement)
        project.messageBus.connect(this).subscribe(XBreakpointListener.TOPIC, SetXFieldWatchpointsAchievement)
        project.messageBus.connect(this).subscribe(XBreakpointListener.TOPIC, SetXLineBreakpointsAchievement)
        project.messageBus.connect(this).subscribe(XBreakpointListener.TOPIC, SetXMethodBreakpointsAchievement)
        project.messageBus.connect(this).subscribe(SMTRunnerEventsListener.TEST_STATUS, FindXBugsAchievement)
        project.messageBus.connect(this).subscribe(SMTRunnerEventsListener.TEST_STATUS, RepairXWrongTestsAchievement)

        project.messageBus.connect(this).subscribe(VirtualFileManager.VFS_CHANGES, RefactorAddXAssertionsAchievement)
        project.messageBus.connect(this).subscribe(VirtualFileManager.VFS_CHANGES, AddTestsAchievement)
        project.messageBus.connect(this).subscribe(VirtualFileManager.VFS_CHANGES, BulkFileListenerImpl)
        project.messageBus.connect(this).subscribe(ExecutionManager.EXECUTION_TOPIC, ConsoleListener)

        CoverageDataManager.getInstance(project).addSuiteListener(CoverageListener, this)
    }

    override fun dispose() = Unit
}
