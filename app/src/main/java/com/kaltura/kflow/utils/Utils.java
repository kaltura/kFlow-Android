package com.kaltura.kflow.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import androidx.annotation.Nullable;

public class Utils {

    public static void hideKeyboard(@Nullable View view) {
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getRootView().getWindowToken(), 0);
        }
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected();
    }

    public static String getUUID(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        UUID uuid;
        try {
            if (!"9774d56d682e549c".equals(androidId)) {
                uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
            } else {
                String deviceId = ((TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE))
                        .getDeviceId();
                uuid = UUID.nameUUIDFromBytes(deviceId.getBytes("utf8"));
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return uuid.toString();
    }

    /**
     * Converts a time given in seconds in the format hh:mm:ss
     *
     * @param Time given in seconds
     * @return Time received in the format hh:mm:ss
     */
    public static String durationInSecondsToString(int sec) {
        int hours = sec / 3600;
        int minutes = (sec / 60) - (hours * 60);
        int seconds = sec - (hours * 3600) - (minutes * 60);
        if (hours < 0) {
            hours = 0;
        }
        if (minutes < 0) {
            minutes = 0;
        }
        if (seconds < 0) {
            seconds = 0;
        }
        String formatted = String.format("%d:%02d:%02d", hours, minutes, seconds);
        return formatted;
    }

    /**
     * Rounds the specified value
     *
     * @param A given bitrate
     * @return Rounded in a given bit rate defined format: if bitrate > 1000 =>
     * bitrate(mb) else bitrate(kb)
     */
    public static StringBuffer roundBitrate(int bitrate) {
        int roundBitrate = Math.round(bitrate / 100) * 100;
        StringBuffer formatted = new StringBuffer();
        if (roundBitrate / 1000 == 0) {
            //Kb
            formatted.append(roundBitrate);
            formatted.append("kb");
        } else {
            //Mb
            formatted.append(roundBitrate / 1000.0);
            formatted.append("mb");
        }
        return formatted;
    }
}
