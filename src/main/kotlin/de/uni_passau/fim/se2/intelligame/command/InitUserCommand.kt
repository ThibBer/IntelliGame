package de.uni_passau.fim.se2.intelligame.command

import de.uni_passau.fim.se2.intelligame.leaderboard.User

data class InitUsersCommand(
    val action: String,
    val payload: List<User>
)