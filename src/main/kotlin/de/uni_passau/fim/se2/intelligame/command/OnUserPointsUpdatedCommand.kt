package de.uni_passau.fim.se2.intelligame.command

import de.uni_passau.fim.se2.intelligame.leaderboard.User

data class OnUserPointsUpdatedCommand(val action: String, val payload: OnUserPointsUpdatedCommandData)
data class OnUserPointsUpdatedCommandData(val user: User, val earnedPoints: Int)