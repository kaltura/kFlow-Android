package com.kaltura.kflow.presentation.player;

import android.os.Handler;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by alex_lytvynenko on 2019-10-28.
 */
class PlayerKeepAliveService {

    private final static String TAG = PlayerKeepAliveService.class.getCanonicalName();

    private final long KEEP_ALIVE_CYCLE = 10000L;
    private Handler scheduler = null;
    private String keepAliveURL;

    PlayerKeepAliveService() {
        keepAliveURL = "";
        if (scheduler == null) {
            scheduler = new Handler();
        }
        scheduler.removeCallbacks(fireKeepAliveCallsRunnable);
    }

    void startFireKeepAliveService() {
        if (scheduler != null) {
            cancelFireKeepAliveService();
            fireKeepAliveHeaderUrl(keepAliveURL);
            scheduler.postDelayed(fireKeepAliveCallsRunnable, KEEP_ALIVE_CYCLE);
        }
    }

    void cancelFireKeepAliveService() {
        if (scheduler != null) {
            scheduler.removeCallbacks(fireKeepAliveCallsRunnable);
        }
    }

    void setKeepAliveURL(String url) {
        keepAliveURL = url;
    }

    private static void fireKeepAliveHeaderUrl(final String url) {
        new Thread(() -> {
            try {
                String keepAliveStr = url + "/" + "keepalive";
                URL firedKAURL = new URL(keepAliveStr);
                HttpURLConnection conn = (HttpURLConnection) firedKAURL.openConnection();
                conn.setInstanceFollowRedirects(false);
                final boolean isSuccess = conn.getResponseCode() == 204;
                if (isSuccess) {
                    Log.d(TAG, "Firing KeepAliveURL : " + firedKAURL);
                } else {
                    Log.d(TAG, "Firing KeepAliveURL failed : " + firedKAURL);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    private Runnable fireKeepAliveCallsRunnable = () -> {
        startFireKeepAliveService();
    };
}
