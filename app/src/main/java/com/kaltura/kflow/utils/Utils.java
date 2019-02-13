package com.kaltura.kflow.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.kaltura.client.types.Asset;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

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

    public static File saveToFile(Context context, String text) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String logFileName = "file_" + timeStamp;
        File logFile;
        try {
            logFile = File.createTempFile(
                    logFileName,           /* prefix */
                    ".txt",         /* suffix */
                    context.getCacheDir()      /* directory */
            );
            FileOutputStream fileOutputStream = new FileOutputStream(logFile, true);
            fileOutputStream.write(text.getBytes());
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            logFile = new File(logFileName);
        }
        return logFile;
    }

    public static void shareFile(Activity activity, File file) {
        if (file.exists()) {
            Intent intentShareFile = new Intent(Intent.ACTION_SEND);
            intentShareFile.setType("text/plain");
            Uri fileUri;
            if (Build.VERSION.SDK_INT > 21) {
                fileUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", file);
            } else {
                fileUri = Uri.fromFile(file);
            }

            intentShareFile.putExtra(Intent.EXTRA_STREAM, fileUri);
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing request data");
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing request data...");
            activity.startActivity(Intent.createChooser(intentShareFile, "Share request data"));
        }
    }

    public static long utcToLocal(long utcTime) {
        return utcTime + TimeZone.getDefault().getOffset(utcTime);
    }

    public static boolean isProgramIsPast(Asset asset) {
        return asset.getEndDate() < Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() / 1000;
    }
}
