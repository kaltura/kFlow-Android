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
import kotlinx.android.synthetic.main.fragment_vod.*

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class GetVodFragment : DebugFragment(R.layout.fragment_vod) {

    private val assets = arrayListOf<Asset>()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAssets.navigateOnClick(GetVodFragmentDirections.navigateToAssetList()) { arrayOf(AssetListFragment.ARG_ASSETS to assets) }
        get.setOnClickListener {
            hideKeyboard()
            makeGetVodRequest(ksqlRequest.string)
        }
        ksqlRequest.string = "(or name~\'Bigg Boss S12\')"
        showAssets.visibleOrGone(assets.isNotEmpty())
        showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
    }

    override fun subscribeUI() {}

    private fun makeGetVodRequest(kSqlRequest: String) {
        withInternetConnection {
            assets.clear()
            showAssets.gone()
            val filter = SearchAssetFilter().apply {
                orderBy = AssetOrderBy.START_DATE_DESC.value
                kSql = kSqlRequest
            }

            val filterPager = FilterPager().apply {
                pageIndex = 1
                pageSize = 50
            }
            clearDebugView()
            PhoenixApiManager.execute(AssetService.list(filter, filterPager).setCompletion {
                if (it.isSuccess) {
                    if (it.results.objects != null) assets.addAll(it.results.objects)
                    showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                    showAssets.visible()
                }
            })
        }
    }
}