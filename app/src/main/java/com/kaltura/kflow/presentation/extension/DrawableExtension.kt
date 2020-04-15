package com.kaltura.kflow.presentation.extension

import android.graphics.drawable.Drawable
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat

/**
 * Created by alex_lytvynenko on 06.08.2018.
 */

fun Drawable.setColor(color: Int) {
    colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(color, BlendModeCompat.SRC_ATOP)
}