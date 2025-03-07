package de.uni_passau.fim.se2.intelligame;

import de.uni_passau.fim.se2.intelligame.util.Util

class GenerateSQLAchievements {
    fun main(args: Array<String>) {
        val achievements = Util.getAchievements()
        for (achievement in achievements) {
            println("INSERT INTO achievement (id, name, description) VALUES('${achievement::class.simpleName}', '${achievement.getName()}', '${achievement.getDescription()}');")
        }
    }
}
