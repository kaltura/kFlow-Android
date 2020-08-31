package com.kaltura.kflow.utils

import android.content.Context
import android.net.ConnectivityManager
import android.provider.Settings
import android.telephony.TelephonyManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*

fun hasInternetConnection(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isAvailable && cm.activeNetworkInfo.isConnected
}

fun getUUID(context: Context): String {
    val androidId = Settings.Secure.getString(context.contentResolver,
            Settings.Secure.ANDROID_ID)
    val uuid: UUID
    uuid = try {
        if ("9774d56d682e549c" != androidId) {
            UUID.nameUUIDFromBytes(androidId.toByteArray(charset("utf8")))
        } else {
            val deviceId = (context
                    .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager)
                    .deviceId
            UUID.nameUUIDFromBytes(deviceId.toByteArray(charset("utf8")))
        }
    } catch (e: UnsupportedEncodingException) {
        throw RuntimeException(e)
    }
    return uuid.toString()
}

/**
 * Converts a time given in seconds in the format hh:mm:ss
 *
 * @param Time given in seconds
 * @return Time received in the format hh:mm:ss
 */
fun durationInSecondsToString(sec: Int): String {
    var hours = sec / 3600
    var minutes = sec / 60 - hours * 60
    var seconds = sec - hours * 3600 - minutes * 60
    if (hours < 0) {
        hours = 0
    }
    if (minutes < 0) {
        minutes = 0
    }
    if (seconds < 0) {
        seconds = 0
    }
    return String.format("%d:%02d:%02d", hours, minutes, seconds)
}

fun saveToFile(context: Context, text: String): File {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val logFileName = "file_$timeStamp"
    var logFile: File
    try {
        logFile = File.createTempFile(
                logFileName,  /* prefix */
                ".txt",  /* suffix */
                context.cacheDir /* directory */
        )
        val fileOutputStream = FileOutputStream(logFile, true)
        fileOutputStream.write(text.toByteArray())
        fileOutputStream.close()
    } catch (e: IOException) {
        e.printStackTrace()
        logFile = File(logFileName)
    }
    return logFile
}

fun utcToLocal(utcTime: Long): Long {
    return utcTime + TimeZone.getDefault().getOffset(utcTime)
}