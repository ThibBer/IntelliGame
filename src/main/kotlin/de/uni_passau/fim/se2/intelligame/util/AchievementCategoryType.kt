package de.uni_passau.fim.se2.intelligame.util

enum class AchievementCategoryType {
    TESTING, COVERAGE, COVERAGE_ADVANCED, DEBUGGING, TEST_REFACTORING;

    override fun toString(): String {
        return when (this) {
            TESTING -> "Testing"
            COVERAGE -> "Coverage"
            COVERAGE_ADVANCED -> "Coverage - Advanced"
            DEBUGGING -> "Debugging"
            TEST_REFACTORING -> "Test Refactoring"
        }
    }
}