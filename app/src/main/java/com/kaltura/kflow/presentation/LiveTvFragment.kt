package com.kaltura.kflow.presentation

import android.os.Bundle
import android.view.View
import com.kaltura.client.enums.AssetOrderBy
import com.kaltura.client.services.AssetService
import com.kaltura.client.types.Asset
import com.kaltura.client.types.FilterPager
import com.kaltura.client.types.SearchAssetFilter
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.assetList.AssetListFragment
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.MainActivity
import kotlinx.android.synthetic.main.fragment_live.*

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
class LiveTvFragment : DebugFragment(R.layout.fragment_live) {

    private val channels = arrayListOf<Asset>()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Live TV"

        showChannel.setOnClickListener {
            hideKeyboard()
            replaceFragment(instanceOf<AssetListFragment>(AssetListFragment.ARG_ASSETS to channels), addToBackStack = true)
        }
        get.setOnClickListener {
            hideKeyboard()
            makeGetChannelsRequest(channelName.string)
        }
        channelName.string = "Отр"
        showChannel.visibleOrGone(channels.isNotEmpty())
        showChannel.text = getQuantityString(R.plurals.show_channels, channels.size)
    }

    private fun makeGetChannelsRequest(channelName: String) {
        withInternetConnection {
            channels.clear()
            showChannel.gone()
            val filter = SearchAssetFilter().apply {
                orderBy = AssetOrderBy.START_DATE_DESC.value
                name = channelName
                kSql = "(and name~'$channelName' (and (and customer_type_blacklist != '5' (or region_agnostic_user_types = '5' (or region_whitelist = '1077' (and region_blacklist != '1077' (or region_whitelist !+ '' region_whitelist = '0'))))) asset_type='600'))"
            }

            val filterPager = FilterPager().apply {
                pageIndex = 1
                pageSize = 50
            }

            clearDebugView()
            PhoenixApiManager.execute(AssetService.list(filter, filterPager).setCompletion {
                if (it.isSuccess) {
                    if (it.results.objects != null) channels.addAll(it.results.objects)
                    showChannel.text = getQuantityString(R.plurals.show_channels, channels.size)
                    showChannel.visible()
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        PhoenixApiManager.cancelAll()
    }
}