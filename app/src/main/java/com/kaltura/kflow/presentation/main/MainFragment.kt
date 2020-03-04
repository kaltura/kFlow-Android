package com.kaltura.kflow.presentation.main

import android.os.Bundle
import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.navigate
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSpringAnimation()
        initList()
    }

    private fun initList() {
        list.setHasFixedSize(true)
        list.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
        val adapter = FeatureAdapter(features)
        adapter.clickListener = {
            navigate(when (it) {
                Feature.LOGIN -> MainFragmentDirections.navigateToLogin()
                Feature.ANONYMOUS_LOGIN -> MainFragmentDirections.navigateToAnonymousLogin()
                Feature.REGISTRATION -> MainFragmentDirections.navigateToRegistration()
                Feature.VOD -> MainFragmentDirections.navigateToVod()
                Feature.EPG -> MainFragmentDirections.navigateToEpg()
                Feature.LIVE -> MainFragmentDirections.navigateToLiveTv()
                Feature.FAVORITES -> MainFragmentDirections.navigateToFavorites()
                Feature.SEARCH -> MainFragmentDirections.navigateToSearch()
                Feature.KEEP_ALIVE -> MainFragmentDirections.navigateToKeepAlive()
                Feature.MEDIA_PAGE -> MainFragmentDirections.navigateToMediaPage()
                Feature.SUBSCRIPTION -> MainFragmentDirections.navigateToSubscription()
                Feature.PRODUCT_PRICE -> MainFragmentDirections.navigateToProductPrice()
                Feature.CHECK_RECEIPT -> MainFragmentDirections.navigateToCheckReceipt()
                Feature.TRANSACTION_HISTORY -> MainFragmentDirections.navigateToTransactionHistory()
                Feature.RECORDINGS -> MainFragmentDirections.navigateToRecordings()
                Feature.SETTINGS -> MainFragmentDirections.navigateToSettings()
            })
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