package de.uni_passau.fim.se2.intelligame.leaderboard.command

import de.uni_passau.fim.se2.intelligame.leaderboard.User


data class OnUserAddedCommand(val action: String, val payload: User)