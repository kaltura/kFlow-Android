package com.kaltura.kflow.presentation.search

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/30/18.
 */
class SearchFragment : SharedTransitionFragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModel()
    private var assets = arrayListOf<Asset>()

    override fun debugView(): DebugView = debugView

    override val feature = Feature.SEARCH

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
        showAssets.navigateOnClick { SearchFragmentDirections.navigateToAssetList(assets = assets.toTypedArray()) }
    }

    override fun subscribeUI() {
        observeResource(viewModel.assets,
                error = { search.error(lifecycleScope) },
                success = {
                    search.success(lifecycleScope)

                    assets = it
                    showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                    showAssets.visible()
                }
        )
        observeResource(viewModel.historyAssetsCount,
                error = { getSearchHistory.error(lifecycleScope) },
                success = {
                    getSearchHistory.success(lifecycleScope)

                    historyCount.text = getQuantityString(R.plurals.history_count, it)
                    historyCount.visible()
                }
        )
    }

    private fun searchRequest(typeInSearch: String, kSqlSearch: String) {
        withInternetConnection {
            historyCount.gone()
            showAssets.gone()
            clearDebugView()
            search.startAnimation {
                viewModel.search(typeInSearch, kSqlSearch)
            }
        }
    }

    private fun searchHistoryRequest() {
        withInternetConnection {
            historyCount.gone()
            clearDebugView()
            getSearchHistory.startAnimation {
                viewModel.searchHistory()
            }
        }
    }
}