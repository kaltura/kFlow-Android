package com.kaltura.kflow.presentation

import android.os.Bundle
import android.view.View
import com.kaltura.client.enums.SearchHistoryOrderBy
import com.kaltura.client.services.AssetService
import com.kaltura.client.services.SearchHistoryService
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.assetList.AssetListFragment
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.synthetic.main.fragment_search.*

/**
 * Created by alex_lytvynenko on 11/30/18.
 */
class SearchFragment : DebugFragment(R.layout.fragment_search) {

    private val assets = arrayListOf<Asset>()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAssets.visibleOrGone(assets.isNotEmpty())
        showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)

        getSearchHistory.setOnClickListener {
            hideKeyboard()
            searchHistoryRequest()
        }
        search.setOnClickListener {
            hideKeyboard()
            searchRequest(typeIn.string, searchText.string)
        }
        showAssets.navigateOnClick(SearchFragmentDirections.navigateToAssetList()) { arrayOf(AssetListFragment.ARG_ASSETS to assets) }
    }

    override fun subscribeUI() {}

    private fun searchRequest(typeInSearch: String, kSqlSearch: String) {
        withInternetConnection {
            assets.clear()
            historyCount.gone()
            showAssets.gone()
            val filter = SearchAssetFilter().apply {
                typeIn = typeInSearch
                kSql = "(or description ~ \'$kSqlSearch \' name ~ \'$kSqlSearch \')"
            }

            val filterPager = FilterPager().apply {
                pageIndex = 1
                pageSize = 50
            }

            PhoenixApiManager.execute(AssetService.list(filter, filterPager).setCompletion {
                if (it.isSuccess) {
                    if (it.results.objects != null) assets.addAll(it.results.objects)
                    showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                    showAssets.visible()
                }
            })
            clearDebugView()
        }
    }

    private fun searchHistoryRequest() {
        withInternetConnection {
            val filter = SearchHistoryFilter().apply { orderBy = SearchHistoryOrderBy.NONE.value }
            val filterPager = FilterPager().apply {
                pageIndex = 1
                pageSize = 50
            }

            historyCount.gone()
            clearDebugView()
            PhoenixApiManager.execute(SearchHistoryService.list(filter, filterPager).setCompletion {
                if (it.isSuccess) {
                    historyCount.text = getQuantityString(R.plurals.history_count, it.results.totalCount)
                    historyCount.visible()
                }
            })
        }
    }
}