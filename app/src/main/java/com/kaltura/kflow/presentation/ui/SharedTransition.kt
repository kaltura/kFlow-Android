package com.kaltura.kflow.presentation.ui

import androidx.transition.*

/**
 * Created by alex_lytvynenko
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
