package com.kaltura.kflow.presentation.player

import android.os.Handler
import android.util.Log
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by alex_lytvynenko on 2019-10-28.
 */
class PlayerKeepAliveService {
    private val TAG = PlayerKeepAliveService::class.java.canonicalName

    private val KEEP_ALIVE_CYCLE = 10000L
    private var scheduler = Handler()
    private val fireKeepAliveCallsRunnable = Runnable { startFireKeepAliveService() }
    var keepAliveURL = ""

    init {
        scheduler.removeCallbacks(fireKeepAliveCallsRunnable)
    }

    fun startFireKeepAliveService() {
        cancelFireKeepAliveService()
        fireKeepAliveHeaderUrl(keepAliveURL)
        scheduler.postDelayed(fireKeepAliveCallsRunnable, KEEP_ALIVE_CYCLE)
    }

    fun cancelFireKeepAliveService() {
        scheduler.removeCallbacks(fireKeepAliveCallsRunnable)
    }

    private fun fireKeepAliveHeaderUrl(url: String) {
        Thread(Runnable {
            try {
                val keepAliveStr = "$url/keepalive"
                val firedKAURL = URL(keepAliveStr)
                val conn = firedKAURL.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                val isSuccess = conn.responseCode == 204

                if (isSuccess) Log.d(TAG, "Firing KeepAliveURL : $firedKAURL")
                else Log.d(TAG, "Firing KeepAliveURL failed : $firedKAURL")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }).start()
    }
}