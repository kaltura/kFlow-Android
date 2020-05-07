package com.kaltura.kflow.utils

import android.content.res.Resources

/**
 * Created by alex_lytvynenko on 02.01.2018.
 */
fun screenHeight(): Int {
    return Resources.getSystem().displayMetrics.heightPixels
}

fun screenWidth(): Int {
    return Resources.getSystem().displayMetrics.widthPixels
}