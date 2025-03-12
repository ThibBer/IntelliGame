package de.uni_passau.fim.se2.intelligame.leaderboard

object Leaderboard {
    private val users = ArrayList<User>()

    init {
        println("Init leaderboard")
    }

    fun addUser(user: User) {
        users.add(user)
        sort()
    }

    private fun removeUser(user: User) {
        users.remove(user)
    }

    fun updateUser(newUserData: User) {
        val user = users.find { it.id == newUserData.id }
        if(user != null) {
            removeUser(user)
            addUser(newUserData)
        }
    }

    fun getUsers(): List<User> {
        return users
    }

    fun setUsers(users: List<User>) {
        reset()

        for(user in users){
            this.users.add(user)
        }

        sort()
    }

    private fun reset(){
        users.clear()
    }

    private fun sort(){
        users.sortByDescending { it.points }
    }
}