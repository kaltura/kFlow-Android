package com.kaltura.kflow.presentation.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentEpgBinding
import com.kaltura.kflow.databinding.FragmentFavoritesBinding
import com.kaltura.kflow.databinding.FragmentSearchBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
class FavoritesFragment : SharedTransitionFragment(R.layout.fragment_favorites) {

    private val viewModel: FavoritesViewModel by viewModel()
    private var assets = arrayListOf<Asset>()

    override val feature = Feature.FAVORITES
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentFavoritesBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.showAssets.navigateOnClick { FavoritesFragmentDirections.navigateToAssetList(assets = assets.toTypedArray()) }
        binding.getFavorites.setOnClickListener {
            hideKeyboard()
            getFavoritesRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.getAssetList,
                error = { binding.getFavorites.error(lifecycleScope) },
                success = {
                    binding.getFavorites.success(lifecycleScope)
                    assets = it
                    binding.showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                    binding.showAssets.visible()
                }
        )
    }

    private fun getFavoritesRequest() {
        withInternetConnection {
            binding.showAssets.gone()
            clearDebugView()
            binding.getFavorites.startAnimation {
                viewModel.getFavorites()
            }
        }
    }
}