package de.uni_passau.fim.se2.intelligame.leaderboard.command

import de.uni_passau.fim.se2.intelligame.leaderboard.User

class UserData {
    data class InitUsers(
        val action: String,
        val payload: List<User>
    )
}
