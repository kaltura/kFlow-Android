package com.kaltura.kflow.presentation.liveTv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.assetList.AssetListFragment
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.synthetic.main.fragment_live.*

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
class LiveTvFragment : DebugFragment(R.layout.fragment_live) {

    private val viewModel: LiveTvViewModel by viewModels()
    private var channels = arrayListOf<Asset>()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showChannel.navigateOnClick(LiveTvFragmentDirections.navigateToAssetList()) { arrayOf(AssetListFragment.ARG_ASSETS to channels) }
        get.setOnClickListener {
            hideKeyboard()
            makeGetChannelsRequest(channelName.string)
        }
        channelName.string = "Отр"
    }

    override fun subscribeUI() {
        observeResource(viewModel.channelList) {
            channels = it
            showChannel.text = getQuantityString(R.plurals.show_channels, channels.size)
            showChannel.visible()
        }
    }

    private fun makeGetChannelsRequest(channelName: String) {
        withInternetConnection {
            showChannel.gone()
            clearDebugView()
            viewModel.getChannels(channelName)
        }
    }
}