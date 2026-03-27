package com.example.atdd.session

class SessionManager(private val sessionRepository: SessionRepository) {

    fun isLoggedIn(): Boolean = sessionRepository.getToken() != null

    fun saveSession(token: String, displayName: String) {
        sessionRepository.saveToken(token)
        sessionRepository.saveDisplayName(displayName)
    }

    fun getDisplayName(): String = sessionRepository.getDisplayName().orEmpty()

    fun clearSession() {
        sessionRepository.clear()
    }
}
