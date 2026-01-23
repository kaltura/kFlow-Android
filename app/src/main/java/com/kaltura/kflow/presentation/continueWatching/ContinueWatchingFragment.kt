package com.kaltura.kflow.presentation.continueWatching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentCollectionsBinding
import com.kaltura.kflow.databinding.FragmentContinueWatchingBinding
import com.kaltura.kflow.databinding.FragmentRegistrationBinding
import com.kaltura.kflow.entity.WatchedAsset
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
class ContinueWatchingFragment : SharedTransitionFragment(R.layout.fragment_continue_watching) {

    private val viewModel: ContinueWatchingViewModel by viewModel()
    private var assets = arrayListOf<WatchedAsset>()

    override val feature = Feature.CONTINUE_WATCHING
    private var _binding: FragmentContinueWatchingBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentContinueWatchingBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.showAssets.navigateOnClick { ContinueWatchingFragmentDirections.navigateToAssetList(watchedAssets = assets.toTypedArray()) }
        binding.getWatched.setOnClickListener {
            hideKeyboard()
            getFavoritesRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.getWatchedAssetList,
                error = { binding.getWatched.error(lifecycleScope) },
                success = {
                    binding.getWatched.success(lifecycleScope)
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
            binding.getWatched.startAnimation {
                viewModel.getWatchedAssets()
            }
        }
    }
}