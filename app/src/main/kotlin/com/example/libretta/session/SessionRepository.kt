package com.example.libretta.session

interface SessionRepository {
    fun saveToken(token: String)
    fun getToken(): String?
    fun saveDisplayName(displayName: String)
    fun getDisplayName(): String?
    fun clear()
}
