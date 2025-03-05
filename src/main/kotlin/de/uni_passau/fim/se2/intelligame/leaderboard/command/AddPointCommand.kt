package de.uni_passau.fim.se2.intelligame.leaderboard.command

data class AddPointCommand(val action: String, val payload: AddPointCommandData)
data class AddPointCommandData(val id: String, val points: Int, val name: String)
