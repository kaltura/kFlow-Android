package com.kaltura.kflow.presentation.main

import android.os.Bundle
import android.transition.Fade
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager.VERTICAL
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = Fade().setDuration(150L)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view?.doOnPreDraw { startPostponedEnterTransition() }
        initList()
    }

    private fun initList() {
        list.setHasFixedSize(true)
        list.layoutManager = StaggeredGridLayoutManager(2, VERTICAL)
        val adapter = FeatureAdapter(features)
        adapter.clickListener = { feature, image, title, bg ->
            when (feature) {
                Feature.LOGIN -> navigateWithExtras(MainFragmentDirections.navigateToLogin(image.transitionName, title.transitionName,
                        bg.transitionName, bg.background.toBitmap(bg.width, bg.height)),
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
    }
}