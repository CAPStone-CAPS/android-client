package com.example.capstone_2.util

import android.content.Context
import android.content.SharedPreferences

object TokenStore {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACCESS = "accessToken"
    private const val KEY_REFRESH = "refreshToken"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAccessToken(context: Context, token: String) {
        prefs(context).edit().putString(KEY_ACCESS, token).apply()
    }

    fun saveRefreshToken(context: Context, token: String) {
        prefs(context).edit().putString(KEY_REFRESH, token).apply()
    }

    fun getAccessToken(context: Context): String? =
        prefs(context).getString(KEY_ACCESS, null)

    fun getRefreshToken(context: Context): String? =
        prefs(context).getString(KEY_REFRESH, null)

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
