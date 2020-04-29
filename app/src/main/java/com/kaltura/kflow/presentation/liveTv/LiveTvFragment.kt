package com.kaltura.kflow.presentation.liveTv

import android.os.Bundle
import android.view.View
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.synthetic.main.fragment_live.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
class LiveTvFragment : DebugFragment(R.layout.fragment_live) {

    private val viewModel: LiveTvViewModel by viewModel()
    private var channels = arrayListOf<Asset>()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showChannel.navigateOnClick { LiveTvFragmentDirections.navigateToAssetList(assets = channels.toTypedArray()) }
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