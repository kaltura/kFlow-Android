package com.kaltura.kflow.presentation.vod

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.Asset
import com.kaltura.client.types.MediaAsset
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentContinueWatchingBinding
import com.kaltura.kflow.databinding.FragmentTransactionHistoryBinding
import com.kaltura.kflow.databinding.FragmentVodBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class GetVodFragment : SharedTransitionFragment(R.layout.fragment_vod) {

    private var assets = arrayListOf<Asset>()
    private val viewModel: GetVodViewModel by viewModel()

    override val feature = Feature.VOD
    private var _binding: FragmentVodBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentVodBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showAssets.navigateOnClick { GetVodFragmentDirections.navigateToAssetList(assets = assets.toTypedArray()) }
        binding.get.setOnClickListener {
            hideKeyboard()
            makeGetVodRequest(binding.name.string, binding.assetType.string)
        }

        binding.assetType.string = viewModel.getVodAssetType()
    }

    override fun subscribeUI() {
        observeResource(viewModel.getAssetList,
                error = { binding.get.error(lifecycleScope) },
                success = {
                    binding.get.success(lifecycleScope)
                    @Suppress("UNCHECKED_CAST")
                    assets = it.filterIsInstance<MediaAsset>() as ArrayList<Asset>
                    binding.showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                    binding.showAssets.visible()
                })
    }

    private fun makeGetVodRequest(name: String, assetType: String) {
        withInternetConnection {
            clearDebugView()
            binding.showAssets.gone()

            binding.get.startAnimation {
                viewModel.getVodAssetList(name, assetType)
            }
        }
    }
}