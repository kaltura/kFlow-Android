package com.kaltura.kflow.presentation.continueWatching

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.entity.WatchedAsset
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_continue_watching.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
class ContinueWatchingFragment : SharedTransitionFragment(R.layout.fragment_continue_watching) {

    private val viewModel: ContinueWatchingViewModel by viewModel()
    private var assets = arrayListOf<WatchedAsset>()

    override fun debugView(): DebugView = debugView

    override val feature = Feature.CONTINUE_WATCHING

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showAssets.navigateOnClick { ContinueWatchingFragmentDirections.navigateToAssetList(watchedAssets = assets.toTypedArray()) }
        getWatched.setOnClickListener {
            hideKeyboard()
            getFavoritesRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.getWatchedAssetList,
                error = { getWatched.error(lifecycleScope) },
                success = {
                    getWatched.success(lifecycleScope)
                    assets = it
                    showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                    showAssets.visible()
                }
        )
    }

    private fun getFavoritesRequest() {
        withInternetConnection {
            showAssets.gone()
            clearDebugView()
            getWatched.startAnimation {
                viewModel.getWatchedAssets()
            }
        }
    }
}