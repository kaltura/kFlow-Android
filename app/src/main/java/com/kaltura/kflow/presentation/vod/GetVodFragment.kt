package com.kaltura.kflow.presentation.vod

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
import kotlinx.android.synthetic.main.fragment_vod.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class GetVodFragment : SharedTransitionFragment(R.layout.fragment_vod) {

    private var assets = arrayListOf<Asset>()
    private val viewModel: GetVodViewModel by viewModel()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.VOD

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAssets.navigateOnClick(GetVodFragmentDirections.navigateToAssetList()) { arrayOf(AssetListFragment.ARG_ASSETS to assets) }
        get.setOnClickListener {
            hideKeyboard()
            makeGetVodRequest(ksqlRequest.string)
        }
        ksqlRequest.string = "(or name~\'Bigg Boss S12\')"
    }

    override fun subscribeUI() {
        observeResource(viewModel.getAssetList,
                error = { get.error(lifecycleScope) },
                success = {
                    get.success(lifecycleScope)
                    assets = it
                    showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                    showAssets.visible()
                })
    }

    private fun makeGetVodRequest(kSqlRequest: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()
            showAssets.gone()

            get.startAnimation {
                viewModel.getVodAssetList(kSqlRequest)
            }
        }
    }

    private fun clearInputLayouts() {
        kSqlInputLayout.hideError()
    }
}