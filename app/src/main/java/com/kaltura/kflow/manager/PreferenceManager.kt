package com.kaltura.kflow.manager

import android.content.Context

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
class PreferenceManager(private val context: Context) {
    private val KEY_BASE_URL = "prefs_base_url"
    private val KEY_VOD_ASSET_TYPE = "prefs_vod_asset_type"
    private val KEY_PARTNER_ID = "prefs_partner_id"
    private val KEY_MAIN_MEDIA_FILE_FORMAT = "prefs_main_media_file_format"
    private val KEY_URL_TYPE = "prefs_url_type"
    private val KEY_STREAMER_TYPE = "prefs_streamer_type"
    private val KEY_MEDIA_PROTOCOL = "prefs_media_protocol"
    private val KEY_KS = "prefs_ks"
    private val KEY_AUTH_USER = "prefs_auth_user"
    private val KEY_AUTH_PASSWORD = "prefs_auth_password"

    private val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)

    var authUser: String
        get() = prefs.getString(KEY_AUTH_USER, null) ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_USER, value).apply()

    var authPassword: String
        get() = prefs.getString(KEY_AUTH_PASSWORD, null) ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_PASSWORD, value).apply()

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, null) ?: "https://rest-us.ott.kaltura.com/v5_0_3/"
        set(value) = prefs.edit().putString(KEY_BASE_URL, value).apply()

    var vodAssetType: String
        get() = prefs.getString(KEY_VOD_ASSET_TYPE, null) ?: ""
        set(value) = prefs.edit().putString(KEY_VOD_ASSET_TYPE, value).apply()

    var mediaFileFormat: String
        get() = prefs.getString(KEY_MAIN_MEDIA_FILE_FORMAT, null) ?: ""
        set(value) = prefs.edit().putString(KEY_MAIN_MEDIA_FILE_FORMAT, value).apply()

    var partnerId: Int
        get() = prefs.getInt(KEY_PARTNER_ID, 3009)
        set(value) = prefs.edit().putInt(KEY_PARTNER_ID, value).apply()

    var ks: String?
        get() = prefs.getString(KEY_KS, null)
        set(value) = prefs.edit().putString(KEY_KS, value).apply()

    var urlType: String
        get() = prefs.getString(KEY_URL_TYPE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_URL_TYPE, value).apply()

    var streamerType: String
        get() = prefs.getString(KEY_STREAMER_TYPE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_STREAMER_TYPE, value).apply()

    var mediaProtocol: String
        get() = prefs.getString(KEY_MEDIA_PROTOCOL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MEDIA_PROTOCOL, value).apply()

    fun clearKs() {
        prefs.edit().putString(KEY_KS, null).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}