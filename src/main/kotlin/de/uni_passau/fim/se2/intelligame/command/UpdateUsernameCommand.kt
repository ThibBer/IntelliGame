package de.uni_passau.fim.se2.intelligame.command

data class UpdateUsernameCommand(val payload: UpdateUsernameCommandData){
    val action = "updateUsername"
}
data class UpdateUsernameCommandData(val id: String, val username: String)