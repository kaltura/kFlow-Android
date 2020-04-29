package com.kaltura.kflow.presentation.vod

import android.os.Bundle
import android.view.View
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.synthetic.main.fragment_vod.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class GetVodFragment : DebugFragment(R.layout.fragment_vod) {

    private var assets = arrayListOf<Asset>()
    private val viewModel: GetVodViewModel by viewModel()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAssets.navigateOnClick { GetVodFragmentDirections.navigateToAssetList(assets = assets.toTypedArray()) }
        get.setOnClickListener {
            hideKeyboard()
            makeGetVodRequest(ksqlRequest.string)
        }
        ksqlRequest.string = "(or name~\'Bigg Boss S12\')"
    }

    override fun subscribeUI() {
        observeResource(viewModel.getAssetList) {
            assets = it
            showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
            showAssets.visible()
        }
    }

    private fun makeGetVodRequest(kSqlRequest: String) {
        withInternetConnection {
            showAssets.gone()
            clearDebugView()
            viewModel.getVodAssetList(kSqlRequest)
        }
    }
}