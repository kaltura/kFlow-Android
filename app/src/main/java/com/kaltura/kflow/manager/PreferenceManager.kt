package com.kaltura.kflow.manager

import android.content.Context
import com.kaltura.kflow.R

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
class PreferenceManager(private val context: Context) {
    private val KEY_BASE_URL = "prefs_base_url"
    private val KEY_CLOUDFRONT_URL = "prefs_cloudfront_url"
    private val KEY_VOD_ASSET_TYPE = "prefs_vod_asset_type"
    private val KEY_PARTNER_ID = "prefs_partner_id"
    private val KEY_MAIN_MEDIA_FILE_FORMAT = "prefs_main_media_file_format"
    private val KEY_URL_TYPE = "prefs_url_type"
    private val KEY_STREAMER_TYPE = "prefs_streamer_type"
    private val KEY_MEDIA_PROTOCOL = "prefs_media_protocol"
    private val KEY_CODEC = "prefs_codec"
    private val KEY_DRM = "prefs_drm"
    private val KEY_QUALITY = "prefs_quality"
    private val KEY_KS = "prefs_ks"
    private val KEY_APP_TOKEN = "prefs_app_token"
    private val KEY_APP_TOKEN_ID = "prefs_app_token_id"
    private val KEY_AUTH_USER = "prefs_auth_user"
    private val KEY_AUTH_PASSWORD = "prefs_auth_password"
    private val KEY_IOT_THING = "prefs_iot_thing"
    private val KEY_IOT_ENDPOINT = "prefs_iot_endpoint"
    private val KEY_IOT_USERNAME = "prefs_iot_username"
    private val KEY_IOT_PASSWORD = "prefs_iot_password"

    private val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)

    var authUser: String
        get() = prefs.getString(KEY_AUTH_USER, null) ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_USER, value).apply()

    var authPassword: String
        get() = prefs.getString(KEY_AUTH_PASSWORD, null) ?: ""
        set(value) = prefs.edit().putString(KEY_AUTH_PASSWORD, value).apply()

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, null) ?: context.getString(R.string.default_base_url)
        set(value) = prefs.edit().putString(KEY_BASE_URL, value).apply()

    var cloudeFrontUrl: String
        get() = prefs.getString(KEY_CLOUDFRONT_URL, null) ?: context.getString(R.string.default_cloudfront_url)
        set(value) = prefs.edit().putString(KEY_CLOUDFRONT_URL, value).apply()


    var vodAssetType: String
        get() = prefs.getString(KEY_VOD_ASSET_TYPE, null) ?: ""
        set(value) = prefs.edit().putString(KEY_VOD_ASSET_TYPE, value).apply()

    var mediaFileFormat: String
        get() = prefs.getString(KEY_MAIN_MEDIA_FILE_FORMAT, null)
                ?: context.getString(R.string.default_media_file_format)
        set(value) = prefs.edit().putString(KEY_MAIN_MEDIA_FILE_FORMAT, value).apply()

    var partnerId: Int
        get() = prefs.getInt(KEY_PARTNER_ID, context.resources.getInteger(R.integer.default_partner_id))
        set(value) = prefs.edit().putInt(KEY_PARTNER_ID, value).apply()

    var ks: String?
        get() = prefs.getString(KEY_KS, null)
        set(value) = prefs.edit().putString(KEY_KS, value).apply()

    var appToken: String?
        get() = prefs.getString(KEY_APP_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_APP_TOKEN, value).apply()

    var appTokenId: String?
        get() = prefs.getString(KEY_APP_TOKEN_ID, null)
        set(value) = prefs.edit().putString(KEY_APP_TOKEN_ID, value).apply()

    var urlType: String
        get() = prefs.getString(KEY_URL_TYPE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_URL_TYPE, value).apply()

    var streamerType: String
        get() = prefs.getString(KEY_STREAMER_TYPE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_STREAMER_TYPE, value).apply()

    var mediaProtocol: String
        get() = prefs.getString(KEY_MEDIA_PROTOCOL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_MEDIA_PROTOCOL, value).apply()

    var codec: String
        get() = prefs.getString(KEY_CODEC, "HEVC") ?: "HEVC"
        set(value) = prefs.edit().putString(KEY_CODEC, value).apply()

    var drm: Boolean
        get() = prefs.getBoolean(KEY_DRM, false)
        set(value) = prefs.edit().putBoolean(KEY_DRM, value).apply()

    var quality: String
        get() = prefs.getString(KEY_QUALITY, "HD") ?: "HD"
        set(value) = prefs.edit().putString(KEY_QUALITY, value).apply()

    var iotThing: String
        get() = prefs.getString(KEY_IOT_THING, null) ?: ""
        set(value) = prefs.edit().putString(KEY_IOT_THING, value).apply()

    var iotEndpoint: String
        get() = prefs.getString(KEY_IOT_ENDPOINT, null) ?: ""
        set(value) = prefs.edit().putString(KEY_IOT_ENDPOINT, value).apply()

    var iotUsername: String
        get() = prefs.getString(KEY_IOT_USERNAME, null) ?: ""
        set(value) = prefs.edit().putString(KEY_IOT_USERNAME, value).apply()

    var iotPassword: String
        get() = prefs.getString(KEY_IOT_PASSWORD, null) ?: ""
        set(value) = prefs.edit().putString(KEY_IOT_PASSWORD, value).apply()

    fun clearIotInfo() {
        prefs.edit().putString(KEY_IOT_THING, null).apply()
        prefs.edit().putString(KEY_IOT_ENDPOINT, null).apply()
        prefs.edit().putString(KEY_IOT_USERNAME, null).apply()
        prefs.edit().putString(KEY_IOT_PASSWORD, null).apply()
    }

    fun clearKs() {
        prefs.edit().putString(KEY_KS, null).apply()
        prefs.edit().putString(KEY_APP_TOKEN, null).apply()
        prefs.edit().putString(KEY_APP_TOKEN_ID, null).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}