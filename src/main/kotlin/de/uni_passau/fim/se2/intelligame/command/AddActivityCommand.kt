package de.uni_passau.fim.se2.intelligame.command

data class AddActivityCommand(val payload: AddActivityCommandData){
    val action = "addActivity"
}
data class AddActivityCommandData(val id: String, val points: Int, val achievement: String, val gameMode: Int)
