package com.pietropuluche.veciapp.data.local

import android.content.Context
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("veciapp_session", Context.MODE_PRIVATE)

    fun saveSession(userId: Long, fullName: String, token: String) {
        prefs.edit {
            putLong(KEY_USER_ID, userId)
            putString(KEY_FULL_NAME, fullName)
            putString(KEY_TOKEN, token)
        }
    }

    fun clear() {
        prefs.edit { clear() }
    }

    fun token(): String = prefs.getString(KEY_TOKEN, "")?.trim().orEmpty()

    fun userId(): Long = prefs.getLong(KEY_USER_ID, -1L)

    fun fullName(): String = prefs.getString(KEY_FULL_NAME, "")?.trim().orEmpty()

    fun isLoggedIn(): Boolean = token().isNotBlank() && userId() > 0

    companion object {
        private const val KEY_USER_ID = "user_id"
        private const val KEY_FULL_NAME = "full_name"
        private const val KEY_TOKEN = "token"
    }
}
