package com.example.libretta.auth

import com.example.libretta.session.SessionManager

object AuthSkipper {

    private const val DEV_TOKEN = "dev-token"
    private const val DEV_DISPLAY_NAME = "開発ユーザー"

    fun applyIfNeeded(skipAuth: Boolean, sessionManager: SessionManager): Boolean {
        if (!skipAuth) return false
        if (!sessionManager.isLoggedIn()) {
            sessionManager.saveSession(DEV_TOKEN, DEV_DISPLAY_NAME)
        }
        return true
    }
}
