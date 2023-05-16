package de.uni_passau.fim.se2.intelligame.listeners

import com.intellij.openapi.project.ProjectLocator
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import de.uni_passau.fim.se2.intelligame.achievements.RefactorCodeAchievement
import de.uni_passau.fim.se2.intelligame.achievements.RefactorExtractXMethodsAchievement
import de.uni_passau.fim.se2.intelligame.achievements.RefactorInlineXMethodsAchievement
import de.uni_passau.fim.se2.intelligame.achievements.RefactorXTestNamesAchievement
import gr.uom.java.xmi.UMLModel
import gr.uom.java.xmi.UMLModelASTReader
import gr.uom.java.xmi.diff.ExtractOperationRefactoring
import gr.uom.java.xmi.diff.InlineOperationRefactoring
import gr.uom.java.xmi.diff.RenameOperationRefactoring
import gr.uom.java.xmi.diff.UMLModelDiff
import org.refactoringminer.api.Refactoring
import java.io.File


object BulkFileListenerImpl : BulkFileListener {
    private var filesUnderObservation = hashMapOf<String, UMLModel>()

    override fun before(events: MutableList<out VFileEvent>) {
        for (event in events) {
            if (event.path.endsWith("Test.java")) {
                val file = File(event.path)
                if (file.exists()) {
                    val folder = file.parentFile
                    val modelBefore: UMLModel = UMLModelASTReader(folder).umlModel
                    filesUnderObservation[folder.path] = modelBefore
                }
            }
        }
        super.before(events)
    }

    override fun after(events: MutableList<out VFileEvent>) {
        for (event in events) {
            if (event.path.endsWith("Test.java")) {
                val file = File(event.path)
                if (file.exists()) {
                    val folder = file.parentFile
                    val modelAfter: UMLModel = UMLModelASTReader(folder).umlModel
                    val modelBefore = filesUnderObservation[folder.path]
                    val modelDiff: UMLModelDiff? = modelBefore?.diff(modelAfter)
                    val refactorings: List<Refactoring> = modelDiff?.refactorings ?: listOf()
                    val project = event.file?.let { ProjectLocator.getInstance().guessProjectForFile(it) }
                    for (refactoring in refactorings) {
                        when (refactoring) {
                            is RenameOperationRefactoring -> {
                                if (!refactoring.renamedOperation.name.contains("\\d".toRegex()))
                                RefactorXTestNamesAchievement.triggerAchievement(project)
                            }
                            is ExtractOperationRefactoring -> {
                                RefactorExtractXMethodsAchievement.triggerAchievement(project)
                            }
                            is InlineOperationRefactoring -> {
                                RefactorInlineXMethodsAchievement.triggerAchievement(project)
                            }
                            else -> {
                                RefactorCodeAchievement.triggerAchievement(project)
                            }
                        }
                    }
                }
            }
        }
        super.after(events)
    }

}