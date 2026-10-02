package com.priti.dailykit.storage

import android.content.Context
import android.content.SharedPreferences

class DailyKitPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "dailykit_user_prefs"
        private const val KEY_FAVORITES = "key_favorites"
        private const val KEY_RECENTS = "key_recents"
        private const val KEY_CURRENCY = "key_currency"
        private const val DEFAULT_CURRENCY = "₹"
        private const val MAX_RECENTS = 10

        @Volatile
        private var instance: DailyKitPreferences? = null

        fun getInstance(context: Context): DailyKitPreferences {
            return instance ?: synchronized(this) {
                instance ?: DailyKitPreferences(context.applicationContext).also { instance = it }
            }
        }
    }

    fun getFavorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun isFavorite(toolId: String): Boolean {
        return getFavorites().contains(toolId)
    }

    fun toggleFavorite(toolId: String): Boolean {
        val current = getFavorites().toMutableSet()
        val newState = if (current.contains(toolId)) {
            current.remove(toolId)
            false
        } else {
            current.add(toolId)
            true
        }
        prefs.edit().putStringSet(KEY_FAVORITES, current).apply()
        return newState
    }

    fun getRecentToolIds(): List<String> {
        val raw = prefs.getString(KEY_RECENTS, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(",").filter { it.isNotBlank() }
    }

    fun addRecentTool(toolId: String) {
        val list = getRecentToolIds().toMutableList()
        list.remove(toolId)
        list.add(0, toolId)
        val trimmed = if (list.size > MAX_RECENTS) list.subList(0, MAX_RECENTS) else list
        prefs.edit().putString(KEY_RECENTS, trimmed.joinToString(",")).apply()
    }

    fun clearRecentTools() {
        prefs.edit().remove(KEY_RECENTS).apply()
    }

    fun getCurrency(): String {
        return prefs.getString(KEY_CURRENCY, DEFAULT_CURRENCY) ?: DEFAULT_CURRENCY
    }

    fun setCurrency(currency: String) {
        prefs.edit().putString(KEY_CURRENCY, currency).apply()
    }
}
