package com.kaltura.kflow.presentation.extension

import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleCoroutineScope
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import br.com.simplepass.loadingbutton.presentation.State
import com.kaltura.kflow.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by alex_lytvynenko on 05.03.2020.
 */

fun CircularProgressButton.error(lifecycleScope: LifecycleCoroutineScope) {
    if (getState() == State.PROGRESS) {
        doneLoadingAnimation(getColor(R.color.red), getDrawable(R.drawable.ic_error_outline_white_24dp)!!.toBitmap())
        lifecycleScope.launch {
            delay(1200)
            revertAnimation()
        }
    }
}

fun CircularProgressButton.success(lifecycleScope: LifecycleCoroutineScope) {
    if (getState() == State.PROGRESS) {
        doneLoadingAnimation(getColor(R.color.green), getDrawable(R.drawable.ic_done_white_24dp)!!.toBitmap())
        lifecycleScope.launch {
            delay(1200)
            revertAnimation()
        }
    }
}