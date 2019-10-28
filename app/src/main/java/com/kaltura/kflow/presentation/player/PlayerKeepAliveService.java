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
    private int pauseBufferLength;
    private Handler scheduler = null;
    private String keepAliveURL;
    private IPlayerState isPlayingState;

    PlayerKeepAliveService(IPlayerState isPlayingState) {
        this.isPlayingState = isPlayingState;
        pauseBufferLength = 0;
        keepAliveURL = "";
        if (scheduler == null) {
            scheduler = new Handler();
        }
        scheduler.removeCallbacksAndMessages(null);
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
            scheduler.removeCallbacksAndMessages(null);
        }
    }

    void setPauseBufferLength(int length) {
        pauseBufferLength = length;
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
        if (!isPlayingState.isPlaying()) {
            startFireKeepAliveService();
        } else {
            if (pauseBufferLength <= 0) {
                cancelFireKeepAliveService();
            } else {
                startFireKeepAliveService();
                pauseBufferLength = pauseBufferLength - (int) KEEP_ALIVE_CYCLE;
                Log.d(TAG, "PAUSE_BUFFER_LENGTH is " + pauseBufferLength);
            }
        }
    };

    interface IPlayerState {
        boolean isPlaying();
    }
}
