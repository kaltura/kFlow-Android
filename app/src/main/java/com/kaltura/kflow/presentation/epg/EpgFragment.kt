package com.kaltura.kflow.presentation.epg

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.assetList.AssetListFragment
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.synthetic.main.fragment_epg.*
import java.util.*

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
class EpgFragment : DebugFragment(R.layout.fragment_epg) {

    private val viewModel: EpgViewModel by viewModels()
    private val channels = ArrayList<Asset>()

    enum class DateFilter {
        YESTERDAY, TODAY, TOMORROW
    }

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showChannel.navigateOnClick(EpgFragmentDirections.navigateToAssetList()) { arrayOf(AssetListFragment.ARG_ASSETS to channels) }
        yesterday.setOnClickListener { makeGetChannelsRequest(linearMediaId.string, DateFilter.YESTERDAY) }
        today.setOnClickListener { makeGetChannelsRequest(linearMediaId.string, DateFilter.TODAY) }
        tomorrow.setOnClickListener { makeGetChannelsRequest(linearMediaId.string, DateFilter.TOMORROW) }

        showChannel.visibleOrGone(channels.isNotEmpty())
        showChannel.text = getQuantityString(R.plurals.show_programs, channels.size)
        channels.clear()
    }

    override fun subscribeUI() {
        observeResource(viewModel.getAssetList) {
            channels.addAll(it)
            showChannel.text = getQuantityString(R.plurals.show_programs, channels.size)
            showChannel.visible()
        }
    }

    private fun makeGetChannelsRequest(epgChannelId: String, dateFilter: DateFilter) {
        withInternetConnection {
            hideKeyboard()
            channels.clear()
            showChannel.gone()
            clearDebugView()
            viewModel.getChannelsRequest(epgChannelId, dateFilter)
        }
    }
}