package com.kaltura.kflow.manager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
public class PreferenceManager {

    private String KEY_BASE_URL = "prefs_base_url";
    private String KEY_PARTNER_ID = "prefs_partner_id";
    private String KEY_MAIN_MEDIA_FILE_FORMAT = "prefs_main_media_file_format";
    private String KEY_KS = "prefs_ks";
    private String KEY_AUTH_USER = "prefs_auth_user";
    private String KEY_AUTH_PASSWORD = "prefs_auth_password";

    private static PreferenceManager sInstance = null;
    private SharedPreferences prefs;

    private PreferenceManager(Context context) {
        prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceManager getInstance(Context context) {
        if (sInstance == null) sInstance = new PreferenceManager(context);
        return sInstance;
    }

    public void saveAuthUser(String username) {
        prefs.edit().putString(KEY_AUTH_USER, username).apply();
    }

    public String getAuthUser() {
        return prefs.getString(KEY_AUTH_USER, "");
    }

    public void saveAuthPassword(String password) {
        prefs.edit().putString(KEY_AUTH_PASSWORD, password).apply();
    }

    public String getAuthPassword() {
        return prefs.getString(KEY_AUTH_PASSWORD, "");
    }

    public void saveBaseUrl(String baseUrl) {
        prefs.edit().putString(KEY_BASE_URL, baseUrl).apply();
    }

    public String getBaseUrl() {
        return prefs.getString(KEY_BASE_URL, "https://restv4-as.ott.kaltura.com/v5_0_3/");
    }

    public void saveMediaFileFormat(String mediaFileFormat) {
        prefs.edit().putString(KEY_MAIN_MEDIA_FILE_FORMAT, mediaFileFormat).apply();
    }

    public String getMediaFileFormat() {
        return prefs.getString(KEY_MAIN_MEDIA_FILE_FORMAT, "Mobile_Dash_SD");
    }

    public void savePartnerId(int partnerId) {
        prefs.edit().putInt(KEY_PARTNER_ID, partnerId).apply();
    }

    public int getPartnerId() {
        return prefs.getInt(KEY_PARTNER_ID, 3065);
    }

    public void saveKs(String ks) {
        prefs.edit().putString(KEY_KS, ks).apply();
    }

    public String getKs() {
        return prefs.getString(KEY_KS, null);
    }

    public void clearKs() {
        prefs.edit().putString(KEY_KS, null).apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
