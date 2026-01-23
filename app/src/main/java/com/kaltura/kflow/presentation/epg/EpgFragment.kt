package com.kaltura.kflow.presentation.epg

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentDeviceManagementBinding
import com.kaltura.kflow.databinding.FragmentEpgBinding
import com.kaltura.kflow.databinding.FragmentLiveBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
class EpgFragment : SharedTransitionFragment(R.layout.fragment_epg) {

    private val viewModel: EpgViewModel by viewModel()
    private var channels = ArrayList<Asset>()
    private var selectedDateFilter = DateFilter.TODAY

    enum class DateFilter {
        YESTERDAY, TODAY, TOMORROW
    }

    override val feature = Feature.EPG
    private var _binding: FragmentEpgBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentEpgBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showChannel.navigateOnClick { EpgFragmentDirections.navigateToAssetList(assets = channels.toTypedArray()) }
        binding.yesterday.setOnClickListener { makeGetChannelsRequest(binding.linearMediaId.string, DateFilter.YESTERDAY) }
        binding.today.setOnClickListener { makeGetChannelsRequest(binding.linearMediaId.string, DateFilter.TODAY) }
        binding.tomorrow.setOnClickListener { makeGetChannelsRequest(binding.linearMediaId.string, DateFilter.TOMORROW) }
    }

    override fun subscribeUI() {
        observeResource(viewModel.getAssetList,
                error = {
                    when (selectedDateFilter) {
                        DateFilter.YESTERDAY -> binding.yesterday
                        DateFilter.TODAY -> binding.today
                        DateFilter.TOMORROW -> binding.tomorrow
                    }.error(lifecycleScope)
                },
                success = {
                    when (selectedDateFilter) {
                        DateFilter.YESTERDAY -> binding.yesterday
                        DateFilter.TODAY -> binding.today
                        DateFilter.TOMORROW -> binding.tomorrow
                    }.success(lifecycleScope)
                    channels = it
                    binding.showChannel.text = getQuantityString(R.plurals.show_programs, channels.size)
                    binding.showChannel.visible()
                })
    }

    private fun makeGetChannelsRequest(epgChannelId: String, dateFilter: DateFilter) {
        withInternetConnection {
            hideKeyboard()
            binding.showChannel.gone()
            clearDebugView()
            clearInputLayouts()
            selectedDateFilter = dateFilter

            if (epgChannelId.isEmpty()) {
                binding.linearMediaIdInputLayout.showError("Empty linear media ID")
                return@withInternetConnection
            }

            when (selectedDateFilter) {
                DateFilter.YESTERDAY -> binding.yesterday
                DateFilter.TODAY -> binding.today
                DateFilter.TOMORROW -> binding.tomorrow
            }.startAnimation {
                viewModel.getChannelsRequest(epgChannelId, selectedDateFilter)
            }
        }
    }

    private fun clearInputLayouts() {
        binding.linearMediaIdInputLayout.hideError()
    }
}