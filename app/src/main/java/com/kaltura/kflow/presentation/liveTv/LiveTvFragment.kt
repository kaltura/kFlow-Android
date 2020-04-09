package com.kaltura.kflow.presentation.liveTv

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.assetList.AssetListFragment
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_live.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
class LiveTvFragment : SharedTransitionFragment(R.layout.fragment_live) {

    private val viewModel: LiveTvViewModel by viewModel()
    private var channels = arrayListOf<Asset>()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.LIVE

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
        observeResource(viewModel.channelList,
                error = { get.error(lifecycleScope) },
                success = {
                    get.success(lifecycleScope)
                    channels = it
                    showChannel.text = getQuantityString(R.plurals.show_channels, channels.size)
                    showChannel.visible()
                })
    }

    private fun makeGetChannelsRequest(channelName: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()
            showChannel.gone()

            get.startAnimation {
                viewModel.getChannels(channelName)
            }
        }
    }

    private fun clearInputLayouts() {
        channelInputLayout.hideError()
    }
}