package com.example.atdd.session

import android.content.Context
import androidx.core.content.edit

class SharedPreferencesSessionRepository(context: Context) : SessionRepository {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override fun saveToken(token: String) {
        prefs.edit { putString(KEY_TOKEN, token) }
    }

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    override fun saveDisplayName(displayName: String) {
        prefs.edit { putString(KEY_DISPLAY_NAME, displayName) }
    }

    override fun getDisplayName(): String? = prefs.getString(KEY_DISPLAY_NAME, null)

    override fun clear() {
        prefs.edit { clear() }
    }

    private companion object {
        const val PREF_NAME = "atdd_session"
        const val KEY_TOKEN = "auth_token"
        const val KEY_DISPLAY_NAME = "user_display_name"
    }
}
