package com.kaltura.kflow.presentation.main

import android.os.Bundle
import android.view.View
import androidx.core.view.doOnPreDraw
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import androidx.transition.Fade
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.navigate
import com.kaltura.kflow.presentation.extension.navigateWithExtras
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class MainFragment : Fragment(R.layout.fragment_main) {

    private val features = arrayOf(Feature.LOGIN, Feature.ANONYMOUS_LOGIN, Feature.REGISTRATION,
            Feature.VOD, Feature.EPG, Feature.LIVE, Feature.FAVORITES, Feature.SEARCH, Feature.KEEP_ALIVE,
            Feature.MEDIA_PAGE, Feature.SUBSCRIPTION, Feature.PRODUCT_PRICE, Feature.CHECK_RECEIPT,
            Feature.TRANSACTION_HISTORY, Feature.RECORDINGS, Feature.SETTINGS)

    private lateinit var rotationAnimation: SpringAnimation
    private var isDragging = false

    private val scrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (isDragging) {
                rotationAnimation.cancel()
                kaltura.rotation += -(dy.toFloat() / 2)
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            isDragging = newState == RecyclerView.SCROLL_STATE_DRAGGING
            if (newState == RecyclerView.SCROLL_STATE_SETTLING) rotationAnimation.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Fade().setDuration(150L)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        initSpringAnimation()
        initList()
    }

    private fun initList() {
        list.setHasFixedSize(true)
        list.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
        val adapter = FeatureAdapter(features)
        adapter.clickListener = { feature, image, title, bg ->
            when (feature) {
                Feature.LOGIN -> navigateWithExtras(MainFragmentDirections.navigateToLogin(image.transitionName, title.transitionName, bg.transitionName),
                        image, title, bg)
                Feature.ANONYMOUS_LOGIN -> navigate(MainFragmentDirections.navigateToAnonymousLogin())
                Feature.REGISTRATION -> navigate(MainFragmentDirections.navigateToRegistration())
                Feature.VOD -> navigate(MainFragmentDirections.navigateToVod())
                Feature.EPG -> navigate(MainFragmentDirections.navigateToEpg())
                Feature.LIVE -> navigate(MainFragmentDirections.navigateToLiveTv())
                Feature.FAVORITES -> navigate(MainFragmentDirections.navigateToFavorites())
                Feature.SEARCH -> navigate(MainFragmentDirections.navigateToSearch())
                Feature.KEEP_ALIVE -> navigate(MainFragmentDirections.navigateToKeepAlive())
                Feature.MEDIA_PAGE -> navigate(MainFragmentDirections.navigateToMediaPage())
                Feature.SUBSCRIPTION -> navigate(MainFragmentDirections.navigateToSubscription())
                Feature.PRODUCT_PRICE -> navigate(MainFragmentDirections.navigateToProductPrice())
                Feature.CHECK_RECEIPT -> navigate(MainFragmentDirections.navigateToCheckReceipt())
                Feature.TRANSACTION_HISTORY -> navigate(MainFragmentDirections.navigateToTransactionHistory())
                Feature.RECORDINGS -> navigate(MainFragmentDirections.navigateToRecordings())
                Feature.SETTINGS -> navigate(MainFragmentDirections.navigateToSettings())
            }
        }
        list.adapter = adapter
        list.addOnScrollListener(scrollListener)
    }

    private fun initSpringAnimation() {
        rotationAnimation = SpringAnimation(kaltura, SpringAnimation.ROTATION).apply {
            spring = SpringForce(0f).apply {
                stiffness = SpringForce.STIFFNESS_LOW
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        list.removeOnScrollListener(scrollListener)
    }
}