package de.uni_passau.fim.se2.intelligame.leaderboard.command

data class UpdateUsernameCommand(val action: String, val payload: UpdateUsernameCommandData)
data class UpdateUsernameCommandData(val id: String, val username: String)