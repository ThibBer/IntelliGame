package de.uni_passau.fim.se2.intelligame.leaderboard.command

data class UserConnectedCommand(val payload: UserConnectedCommandData){
    val action = "userConnected"
}
data class UserConnectedCommandData(val id: String, val username: String)