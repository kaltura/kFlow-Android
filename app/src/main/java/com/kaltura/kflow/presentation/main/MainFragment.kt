package com.kaltura.kflow.presentation.main

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.*
import com.kaltura.kflow.presentation.extension.instanceOf
import com.kaltura.kflow.presentation.extension.replaceFragment
import com.kaltura.kflow.presentation.productPrice.ProductPriceFragment
import com.kaltura.kflow.presentation.subscription.SubscriptionFragment
import com.kaltura.kflow.presentation.transactionHistory.TransactionHistoryFragment
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
        list.layoutManager = LinearLayoutManager(requireContext())
        list.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        val adapter = FeatureAdapter(features)
        adapter.clickListener = {
            replaceFragment(when (it) {
                Feature.LOGIN -> LoginFragment()
                Feature.ANONYMOUS_LOGIN -> AnonymousLoginFragment()
                Feature.REGISTRATION -> RegistrationFragment()
                Feature.VOD -> GetVodFragment()
                Feature.EPG -> EpgFragment()
                Feature.LIVE -> LiveTvFragment()
                Feature.FAVORITES -> FavoritesFragment()
                Feature.SEARCH -> SearchFragment()
                Feature.KEEP_ALIVE -> instanceOf<MediaPageFragment>(MediaPageFragment.ARG_KEEP_ALIVE to true)
                Feature.MEDIA_PAGE -> instanceOf<MediaPageFragment>(MediaPageFragment.ARG_KEEP_ALIVE to false)
                Feature.SUBSCRIPTION -> SubscriptionFragment()
                Feature.PRODUCT_PRICE -> ProductPriceFragment()
                Feature.CHECK_RECEIPT -> CheckReceiptFragment()
                Feature.TRANSACTION_HISTORY -> TransactionHistoryFragment()
                Feature.RECORDINGS -> RecordingsFragment()
                Feature.SETTINGS -> SettingsFragment()
            }, addToBackStack = true)
        }
        list.adapter = adapter
    }
}