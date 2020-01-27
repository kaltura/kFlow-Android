package com.kaltura.kflow.manager

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
object PreferenceManager {
    private const val KEY_BASE_URL = "prefs_base_url"
    private const val KEY_PARTNER_ID = "prefs_partner_id"
    private const val KEY_MAIN_MEDIA_FILE_FORMAT = "prefs_main_media_file_format"
    private const val KEY_DEVICE_PROFILE = "prefs_device_profile"
    private const val KEY_KS = "prefs_ks"
    private const val KEY_AUTH_USER = "prefs_auth_user"
    private const val KEY_AUTH_PASSWORD = "prefs_auth_password"

    private lateinit var prefs: SharedPreferences

    fun with(context: Context): PreferenceManager {
        prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context)
        return this
    }

    var authUser: String
        get() = prefs.getString(KEY_AUTH_USER, null) ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_USER, value).apply()

    var authPassword: String
        get() = prefs.getString(KEY_AUTH_PASSWORD, null) ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_PASSWORD, value).apply()

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, null) ?: "https://api.frs1.ott.kaltura.com"
        set(value) = prefs.edit().putString(KEY_BASE_URL, value).apply()

    var mediaFileFormat: String
        get() = prefs.getString(KEY_MAIN_MEDIA_FILE_FORMAT, null) ?: ""
        set(value) = prefs.edit().putString(KEY_MAIN_MEDIA_FILE_FORMAT, value).apply()

    var deviceProfile: String
        get() = prefs.getString(KEY_DEVICE_PROFILE, null) ?: ""
        set(value) = prefs.edit().putString(KEY_DEVICE_PROFILE, value).apply()

    var partnerId: Int
        get() = prefs.getInt(KEY_PARTNER_ID, 313)
        set(value) = prefs.edit().putInt(KEY_PARTNER_ID, value).apply()

    var ks: String?
        get() = prefs.getString(KEY_KS, null)
        set(value) = prefs.edit().putString(KEY_KS, value).apply()

    fun clearKs() {
        prefs.edit().putString(KEY_KS, null).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}