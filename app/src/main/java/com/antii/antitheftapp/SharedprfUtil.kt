package com.antii.antitheftapp

import android.content.Context


object SharedPrefUtil {

    private const val PREF_NAME = "AntiTheftPrefs"
    private const val KEY_ACTIVE_SERVICES = "active_services"

    fun addActiveService(context: Context, serviceName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_ACTIVE_SERVICES, mutableSetOf())!!.toMutableSet()
        current.add(serviceName)
        prefs.edit().putStringSet(KEY_ACTIVE_SERVICES, current).apply()
    }

    fun removeActiveService(context: Context, serviceName: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_ACTIVE_SERVICES, mutableSetOf())!!.toMutableSet()
        current.remove(serviceName)
        prefs.edit().putStringSet(KEY_ACTIVE_SERVICES, current).apply()
    }

    fun getActiveServices(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_ACTIVE_SERVICES, mutableSetOf()) ?: emptySet()
    }

    fun clearAllActiveServices(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_ACTIVE_SERVICES).apply()
    }
}