package de.uni_passau.fim.se2.intelligame.achievements

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.project.Project

object RefactorXTestNamesAchievement : Achievement() {
    fun triggerAchievement(project: Project?) {
        var progress = progress()
        progress++
        handleProgress(progress, project)
    }

    override fun progress(): Int {
        val properties = PropertiesComponent.getInstance()
        return properties.getInt("RefactorXTestNamesAchievement", 0)
    }

    override fun updateProgress(progress: Int) {
        val properties = PropertiesComponent.getInstance()
        properties.setValue("RefactorXTestNamesAchievement", progress, 0)
    }

    override fun getDescription(): String {
        return "Give your tests better names"
    }

    override fun getName(): String {
        return "The Eponym"
    }

    override fun getStepLevelMatrix(): LinkedHashMap<Int, Int> {
        return linkedMapOf(0 to 10, 1 to 100, 2 to 1000, 3 to 10000)
    }

    override fun supportsLanguages(): List<Language> {
        return listOf(Language.Java)
    }
}