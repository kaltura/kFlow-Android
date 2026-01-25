package com.kaltura.kflow.presentation.liveTv

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.Asset
import com.kaltura.client.types.LiveAsset
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentKsBinding
import com.kaltura.kflow.databinding.FragmentLiveBinding
import com.kaltura.kflow.databinding.FragmentVodBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
class LiveTvFragment : SharedTransitionFragment(R.layout.fragment_live) {

    private val viewModel: LiveTvViewModel by viewModel()
    private var channels = arrayListOf<Asset>()

    override val feature = Feature.LIVE
    private var _binding: FragmentLiveBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentLiveBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showChannel.navigateOnClick { LiveTvFragmentDirections.navigateToAssetList(assets = channels.toTypedArray()) }
        binding.get.setOnClickListener {
            hideKeyboard()
            makeGetChannelsRequest(binding.channelName.string)
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.channelList,
                error = { binding.get.error(lifecycleScope) },
                success = {
                    binding.get.success(lifecycleScope)
                    @Suppress("UNCHECKED_CAST")
                    channels = it.filterIsInstance<LiveAsset>() as ArrayList<Asset>
                    binding.showChannel.text = getQuantityString(R.plurals.show_channels, channels.size)
                    binding.showChannel.visible()
                })
    }

    private fun makeGetChannelsRequest(channelName: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()
            binding.showChannel.gone()

            binding.get.startAnimation {
                viewModel.getChannels(channelName)
            }
        }
    }

    private fun clearInputLayouts() {
        binding.channelInputLayout.hideError()
    }
}