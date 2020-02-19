package com.kaltura.kflow.presentation.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
    }
}