package com.kaltura.kflow.presentation.ui

import android.transition.*

/**
 * Created by alex_lytvynenko on 01.12.17.
 */
class SharedTransition : TransitionSet() {

    init {
        ordering = ORDERING_TOGETHER
        addTransition(ChangeBounds())
                .addTransition(ChangeTransform())
                .addTransition(ChangeImageTransform())
        duration = 500
    }
}