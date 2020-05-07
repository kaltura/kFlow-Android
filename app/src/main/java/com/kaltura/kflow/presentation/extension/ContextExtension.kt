package com.kaltura.kflow.presentation.extension

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import android.view.inputmethod.InputMethodManager
import androidx.annotation.PluralsRes
import java.text.NumberFormat

/**
 * Created by alex_lytvynenko
 */

fun Context.getColor(@ColorRes id: Int) = ContextCompat.getColor(this, id)

fun Context.getDrawable(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)

inline val Context.inputManager: InputMethodManager?
    get() = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager

fun Context.getQuantityString(@PluralsRes id: Int, count: Int): String =
        resources.getQuantityString(id, count, NumberFormat.getInstance().format(count.toLong()))

fun Context.dip(value: Int): Int = (value * resources.displayMetrics.density).toInt()
fun Context.dip(value: Float): Int = (value * resources.displayMetrics.density).toInt()