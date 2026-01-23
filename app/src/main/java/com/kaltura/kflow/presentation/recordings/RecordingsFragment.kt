package com.kaltura.kflow.presentation.recordings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.enums.RecordingStatus
import com.kaltura.client.types.Recording
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentRecordingListBinding
import com.kaltura.kflow.databinding.FragmentRecordingsBinding
import com.kaltura.kflow.databinding.FragmentTransactionHistoryBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
class RecordingsFragment : SharedTransitionFragment(R.layout.fragment_recordings) {

    private val viewModel: RecordingsViewModel by viewModel()
    private var allRecordings = arrayListOf<Recording>()
    private val filteredRecordings = arrayListOf<Recording>()
    private var recordingFilter = RecordingsFilter.RECORDED_FILTER

    private enum class RecordingsFilter {
        RECORDED_FILTER,
        ON_GOING_FILTER,
        SCHEDULED_FILTER
    }

    override val feature = Feature.RECORDINGS
    private var _binding: FragmentRecordingsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentRecordingsBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showRecordings.setOnClickListener {
            hideKeyboard()
            if (filteredRecordings.isNotEmpty()) {
                navigate(RecordingsFragmentDirections.navigateToRecordingList(recordings = filteredRecordings.toTypedArray()))
            }
        }
        binding.getRecorded.setOnClickListener {
            hideKeyboard()
            recordingFilter = RecordingsFilter.RECORDED_FILTER
            recordingsRequest()
        }
        binding.getOnGoing.setOnClickListener {
            hideKeyboard()
            recordingFilter = RecordingsFilter.ON_GOING_FILTER
            recordingsRequest()
        }
        binding.getScheduled.setOnClickListener {
            hideKeyboard()
            recordingFilter = RecordingsFilter.SCHEDULED_FILTER
            recordingsRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.recordingList,
                error = {
                    when (recordingFilter) {
                        RecordingsFilter.RECORDED_FILTER -> binding.getRecorded.error(lifecycleScope)
                        RecordingsFilter.ON_GOING_FILTER -> binding.getOnGoing.error(lifecycleScope)
                        RecordingsFilter.SCHEDULED_FILTER -> binding.getScheduled.error(lifecycleScope)
                    }
                },
                success = {
                    when (recordingFilter) {
                        RecordingsFilter.RECORDED_FILTER -> binding.getRecorded.success(lifecycleScope)
                        RecordingsFilter.ON_GOING_FILTER -> binding.getOnGoing.success(lifecycleScope)
                        RecordingsFilter.SCHEDULED_FILTER -> binding.getScheduled.success(lifecycleScope)
                    }
                    allRecordings = it
                    filterRecordings()
                })
    }

    private fun recordingsRequest() {
        if (allRecordings.isEmpty()) {
            withInternetConnection {
                allRecordings.clear()
                filteredRecordings.clear()
                binding.showRecordings.gone()
                clearDebugView()
                when (recordingFilter) {
                    RecordingsFilter.RECORDED_FILTER -> binding.getRecorded.startAnimation { viewModel.getRecordings() }
                    RecordingsFilter.ON_GOING_FILTER -> binding.getOnGoing.startAnimation { viewModel.getRecordings() }
                    RecordingsFilter.SCHEDULED_FILTER -> binding.getScheduled.startAnimation { viewModel.getRecordings() }
                }
            }
        } else {
            filterRecordings()
        }
    }

    private fun filterRecordings() {
        if (allRecordings.isNotEmpty()) {
            filteredRecordings.clear()
            when (recordingFilter) {
                RecordingsFilter.RECORDED_FILTER -> {
                    allRecordings.forEach {
                        if (it.status == RecordingStatus.RECORDED) filteredRecordings.add(it)
                    }
                    if (filteredRecordings.isEmpty()) binding.showRecordings.setText(R.string.show_empty_recorded)
                    else binding.showRecordings.text = getQuantityString(R.plurals.show_recorded, filteredRecordings.size)
                }
                RecordingsFilter.ON_GOING_FILTER -> {
                    allRecordings.forEach {
                        if (it.status == RecordingStatus.RECORDING) filteredRecordings.add(it)
                    }
                    if (filteredRecordings.isEmpty()) binding.showRecordings.setText(R.string.show_empty_ongoing_recordings)
                    else binding.showRecordings.text = getQuantityString(R.plurals.show_on_going_recording, filteredRecordings.size)
                }
                RecordingsFilter.SCHEDULED_FILTER -> {
                    allRecordings.forEach {
                        if (it.status == RecordingStatus.SCHEDULED) filteredRecordings.add(it)
                    }
                    if (filteredRecordings.isEmpty()) binding.showRecordings.setText(R.string.show_empty_scheduled_recordings)
                    else binding.showRecordings.text = getQuantityString(R.plurals.show_scheduled_recording, filteredRecordings.size)
                }
            }
            binding.showRecordings.visible()
        }
    }
}