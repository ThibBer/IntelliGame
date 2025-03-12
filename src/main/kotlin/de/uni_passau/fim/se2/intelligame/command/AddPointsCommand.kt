package de.uni_passau.fim.se2.intelligame.command

data class AddPointsCommand(val payload: AddPointsCommandData){
    val action = "addPoints"
}
data class AddPointsCommandData(val id: String, val points: Int, val achievement: String, val gameMode: Int)
