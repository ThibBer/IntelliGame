package de.uni_passau.fim.se2.intelligame.listeners

import com.intellij.execution.ExecutionManager
import com.intellij.execution.testframework.AbstractTestProxy
import com.intellij.execution.testframework.TestStatusListener
import com.intellij.openapi.project.Project


class KoverageListener : TestStatusListener() {
    override fun testSuiteFinished(abstractTestProxy: AbstractTestProxy?) {
        println("Suite terminée : ${abstractTestProxy?.magnitude}")
    }

    override fun testSuiteFinished(root: AbstractTestProxy?, project: Project?) {
        super.testSuiteFinished(root, project)
        println("testSuiteFinished ${root?.name}")
    }
}