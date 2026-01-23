package com.kaltura.kflow.presentation.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentEpgBinding
import com.kaltura.kflow.databinding.FragmentReminderListBinding
import com.kaltura.kflow.databinding.FragmentSearchBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/30/18.
 */
class SearchFragment : SharedTransitionFragment(R.layout.fragment_search) {

    private val viewModel: SearchViewModel by viewModel()
    private var assets = arrayListOf<Asset>()

    override val feature = Feature.SEARCH
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSearchBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.getSearchHistory.setOnClickListener {
            hideKeyboard()
            searchHistoryRequest()
        }
        binding.search.setOnClickListener {
            hideKeyboard()
            searchRequest(binding.assetType.string, binding.searchText.string)
        }
        binding.showAssets.navigateOnClick { SearchFragmentDirections.navigateToAssetList(assets = assets.toTypedArray()) }
    }

    override fun subscribeUI() {
        observeResource(viewModel.assets,
                error = { binding.search.error(lifecycleScope) },
                success = {
                    binding.search.success(lifecycleScope)

                    assets = it
                    binding.showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                    binding.showAssets.visible()
                }
        )
        observeResource(viewModel.historyAssetsCount,
                error = { binding.getSearchHistory.error(lifecycleScope) },
                success = {
                    binding.getSearchHistory.success(lifecycleScope)

                    binding.historyCount.text = getQuantityString(R.plurals.history_count, it)
                    binding.historyCount.visible()
                }
        )
    }

    private fun searchRequest(assetType: String, kSqlSearch: String) {
        withInternetConnection {
            binding.historyCount.gone()
            binding.showAssets.gone()
            clearDebugView()
            binding.search.startAnimation {
                viewModel.search(assetType, kSqlSearch)
            }
        }
    }

    private fun searchHistoryRequest() {
        withInternetConnection {
            binding.historyCount.gone()
            clearDebugView()
            binding.getSearchHistory.startAnimation {
                viewModel.searchHistory()
            }
        }
    }
}