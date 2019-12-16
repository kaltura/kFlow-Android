package com.kaltura.kflow.presentation.extension

import android.content.res.TypedArray
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import androidx.annotation.PluralsRes

/**
 * Created by alex_litvinenko
 */

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.isVisible() = this.visibility == View.VISIBLE

fun View.visibleOrGone(visible: Boolean) {
    if (visible) visible() else gone()
}

fun View.visibleOrInvisible(visible: Boolean) {
    if (visible) visible() else invisible()
}

fun View.obtainAttrs(set: AttributeSet?, attrs: IntArray, f: (a: TypedArray) -> Unit) {
    if (set != null) {
        val a = context.obtainStyledAttributes(set, attrs)
        f(a)
        a.recycle()
    }
}

fun View.getColor(@ColorRes id: Int) = ContextCompat.getColor(context, id)

fun View.getDrawable(@DrawableRes id: Int) = ContextCompat.getDrawable(context, id)

fun View.getQuantityString(@PluralsRes id: Int, count: Int): String = context.getQuantityString(id, count)