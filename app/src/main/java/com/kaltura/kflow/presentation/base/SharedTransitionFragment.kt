package com.kaltura.kflow.presentation.base

import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.annotation.LayoutRes
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.kflow.presentation.ui.SharedTransition
import kotlinx.android.synthetic.main.view_shared_transition_header.*
import kotlinx.android.synthetic.main.view_shared_transition_header.view.*

/**
 * Created by alex_lytvynenko on 05.03.2020.
 */
abstract class SharedTransitionFragment(@LayoutRes contentLayoutId: Int) : DebugFragment(contentLayoutId) {

    private var isFirstEnter = true
    abstract val feature: Feature

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = SharedTransition()
        sharedElementReturnTransition = SharedTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (header == null)
            throw IllegalStateException("Fragment must include R.layout.view_shared_transition_header view")

        header.sharedTransitionTitle.transitionName = "${feature.text}_title"
        header.sharedTransitionImage.transitionName = "${feature.text}_image"
        header.sharedTransitionTitle.text = feature.text
        header.sharedTransitionImage.setImageResource(feature.imageResId)
        animateCardEnter()
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
    }

    private fun animateCardEnter() {
        if (isFirstEnter) {
            isFirstEnter = false
            val animation = ScaleAnimation(0f, 1f, 0f, 1f,
                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
                duration = 500
                interpolator = AccelerateDecelerateInterpolator()
            }
            header.sharedTransitionCard.animation = animation
            animation.start()
        }
    }
}