package com.dvir.docsync.core.user

import java.util.concurrent.ConcurrentHashMap

object UserManager {
    private val users = ConcurrentHashMap<String, OnlineUser>()

    fun addUser(user: OnlineUser) {
        users[user.username] = user
    }

    fun removeUser(username: String) {
        users.remove(username)
    }

    fun changeUserState(username: String, newState: UserState) {
        users[username] = users[username]?.copy(state = newState) ?: return
    }

    fun getUser(username: String): OnlineUser? {
        return users[username]
    }

    fun isUserOnline(username: String): Boolean {
        return users.containsKey(username)
    }
}