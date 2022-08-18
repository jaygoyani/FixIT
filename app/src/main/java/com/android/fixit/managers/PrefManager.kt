package com.android.fixit.managers

import android.content.Context
import android.content.SharedPreferences
import com.android.fixit.dtos.UserDTO
import com.android.fixit.utils.FixIt
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PrefManager {
    private const val appPreferences = "app_preferences"
    private const val userPreferences = "user_preferences"

    const val keyUserDetails = "user_details"
    const val keyFirebaseToken = "firebase_token"

    fun getUserSharedPreferences(): SharedPreferences? {
        return FixIt.context.getSharedPreferences(userPreferences, Context.MODE_PRIVATE)
    }

    fun getAppSharedPreferences(): SharedPreferences? {
        return FixIt.context.getSharedPreferences(appPreferences, Context.MODE_PRIVATE)
    }

    fun clearAppPreferences() {
        getAppSharedPreferences()?.edit()?.clear()?.apply()
    }

    fun clearUserPreferences() {
        getUserSharedPreferences()?.edit()?.clear()?.apply()
    }

    fun getUserStringData(key: String): String {
        return getUserSharedPreferences()?.getString(key, null) ?: ""
    }

    fun getUserBooleanData(key: String): Boolean? {
        return getUserSharedPreferences()?.getBoolean(key, false)
    }

    fun getUserLongData(key: String): Long {
        return getUserSharedPreferences()?.getLong(key, 0L) ?: 0L
    }

    fun getAppIntData(key: String): Int {
        return getAppSharedPreferences()?.getInt(key, 0) ?: 0
    }

    fun getAppBooleanData(key: String): Boolean {
        return getAppSharedPreferences()?.getBoolean(key, false) ?: false
    }

    fun getAppStringData(key: String): String? {
        return getAppSharedPreferences()?.getString(key, null)
    }

    fun saveUserData(key: String, value: Any?) {
        val editor = getUserSharedPreferences()?.edit()
        when (value) {
            is String -> editor?.putString(key, value)
            is Boolean -> editor?.putBoolean(key, value)
            is Float -> editor?.putFloat(key, value)
            is Int -> editor?.putInt(key, value)
            is Long -> editor?.putLong(key, value)
        }
        editor?.apply()
    }

    fun saveAppData(key: String, value: Any) {
        val editor = getAppSharedPreferences()?.edit()
        when (value) {
            is String -> editor?.putString(key, value)
            is Boolean -> editor?.putBoolean(key, value)
            is Float -> editor?.putFloat(key, value)
            is Int -> editor?.putInt(key, value)
            is Long -> editor?.putLong(key, value)
        }
        editor?.apply()
    }

    fun containsUserKey(key: String): Boolean {
        return getUserSharedPreferences()?.contains(key) ?: false
    }

    fun saveUserDTO(user: UserDTO) {
        saveUserData(
            keyUserDetails, Gson().toJson(user).toString()
        )
    }

    fun getUserDTO(): UserDTO {
        val str = getUserStringData(keyUserDetails)
        return Gson().fromJson(str, object : TypeToken<UserDTO>() {}.type)
    }

    fun removeUserKey(key: String) {
        val editor = getUserSharedPreferences()?.edit()
        editor?.remove(key)
        editor?.apply()
    }

    fun removeAppKey(key: String) {
        val editor = getAppSharedPreferences()?.edit()
        editor?.remove(key)
        editor?.apply()
    }
}