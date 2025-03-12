package de.uni_passau.fim.se2.intelligame.components

import de.uni_passau.fim.se2.intelligame.achievements.Achievement
import de.uni_passau.fim.se2.intelligame.util.AchievementCategoryType

data class AchievementCategory(val label: AchievementCategoryType, val achievements: List<Achievement>)