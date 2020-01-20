package com.kaltura.kflow.presentation.search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.assetList.AssetListFragment
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.synthetic.main.fragment_search.*

/**
 * Created by alex_lytvynenko on 11/30/18.
 */
class SearchFragment : DebugFragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModels()
    private var assets = arrayListOf<Asset>()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    override fun subscribeUI() {
        observeResource(viewModel.assets) {
            assets = it
            showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
            showAssets.visible()
        }
        observeResource(viewModel.historyAssetsCount) {
            historyCount.text = getQuantityString(R.plurals.history_count, it)
            historyCount.visible()
        }
    }

    private fun searchRequest(typeInSearch: String, kSqlSearch: String) {
        withInternetConnection {
            historyCount.gone()
            showAssets.gone()
            clearDebugView()
            viewModel.search(typeInSearch, kSqlSearch)
        }
    }

    private fun searchHistoryRequest() {
        withInternetConnection {
            historyCount.gone()
            clearDebugView()
            viewModel.searchHistory()
        }
    }
}