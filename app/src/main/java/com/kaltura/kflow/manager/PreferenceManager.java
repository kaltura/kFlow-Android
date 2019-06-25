package com.kaltura.kflow.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.kaltura.kflow.Settings;

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
public class PreferenceManager {

    private String KEY_BASE_URL = "prefs_base_url";
    private String KEY_PARTNER_ID = "prefs_partner_id";
    private String KEY_KS = "prefs_ks";

    private static PreferenceManager sInstance = null;
    private SharedPreferences prefs;

    private PreferenceManager(Context context) {
        prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceManager getInstance(Context context) {
        if (sInstance == null) sInstance = new PreferenceManager(context);
        return sInstance;
    }

    public void saveBaseUrl(String baseUrl) {
        prefs.edit().putString(KEY_BASE_URL, baseUrl).apply();
    }

    public String getBaseUrl() {
        return prefs.getString(KEY_BASE_URL, Settings.host);
    }

    public void savePartnerId(int partnerId) {
        prefs.edit().putInt(KEY_PARTNER_ID, partnerId).apply();
    }

    public int getPartnerId() {
        return prefs.getInt(KEY_PARTNER_ID, Settings.partnerID);
    }

    public void saveKs(String ks) {
        prefs.edit().putString(KEY_KS, ks).apply();
    }

    public String getKs() {
        return prefs.getString(KEY_KS, "");
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
