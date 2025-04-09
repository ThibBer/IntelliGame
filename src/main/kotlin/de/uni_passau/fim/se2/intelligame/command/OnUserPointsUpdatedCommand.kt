package de.uni_passau.fim.se2.intelligame.command

import de.uni_passau.fim.se2.intelligame.leaderboard.User

data class OnUserActivityUpdatedCommand(val action: String, val payload: OnUserActivityUpdatedCommandData)
data class OnUserActivityUpdatedCommandData(val user: User, val earnedPoints: Int)