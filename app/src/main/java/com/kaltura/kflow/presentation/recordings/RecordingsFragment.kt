package com.kaltura.kflow.presentation.recordings

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.enums.RecordingStatus
import com.kaltura.client.types.Recording
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_recordings.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
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

    override fun debugView(): DebugView = debugView
    override val feature = Feature.RECORDINGS

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showRecordings.setOnClickListener {
            hideKeyboard()
            if (filteredRecordings.isNotEmpty()) {
                navigate(RecordingsFragmentDirections.navigateToRecordingList(recordings = filteredRecordings.toTypedArray()))
            }
        }
        getRecorded.setOnClickListener {
            hideKeyboard()
            recordingFilter = RecordingsFilter.RECORDED_FILTER
            recordingsRequest()
        }
        getOnGoing.setOnClickListener {
            hideKeyboard()
            recordingFilter = RecordingsFilter.ON_GOING_FILTER
            recordingsRequest()
        }
        getScheduled.setOnClickListener {
            hideKeyboard()
            recordingFilter = RecordingsFilter.SCHEDULED_FILTER
            recordingsRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.recordingList,
                error = {
                    when (recordingFilter) {
                        RecordingsFilter.RECORDED_FILTER -> getRecorded.error(lifecycleScope)
                        RecordingsFilter.ON_GOING_FILTER -> getOnGoing.error(lifecycleScope)
                        RecordingsFilter.SCHEDULED_FILTER -> getScheduled.error(lifecycleScope)
                    }
                },
                success = {
                    when (recordingFilter) {
                        RecordingsFilter.RECORDED_FILTER -> getRecorded.success(lifecycleScope)
                        RecordingsFilter.ON_GOING_FILTER -> getOnGoing.success(lifecycleScope)
                        RecordingsFilter.SCHEDULED_FILTER -> getScheduled.success(lifecycleScope)
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
                showRecordings.gone()
                clearDebugView()
                when (recordingFilter) {
                    RecordingsFilter.RECORDED_FILTER -> getRecorded.startAnimation { viewModel.getRecordings() }
                    RecordingsFilter.ON_GOING_FILTER -> getOnGoing.startAnimation { viewModel.getRecordings() }
                    RecordingsFilter.SCHEDULED_FILTER -> getScheduled.startAnimation { viewModel.getRecordings() }
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
                    if (filteredRecordings.isEmpty()) showRecordings.setText(R.string.show_empty_recorded)
                    else showRecordings.text = getQuantityString(R.plurals.show_recorded, filteredRecordings.size)
                }
                RecordingsFilter.ON_GOING_FILTER -> {
                    allRecordings.forEach {
                        if (it.status == RecordingStatus.RECORDING) filteredRecordings.add(it)
                    }
                    if (filteredRecordings.isEmpty()) showRecordings.setText(R.string.show_empty_ongoing_recordings)
                    else showRecordings.text = getQuantityString(R.plurals.show_on_going_recording, filteredRecordings.size)
                }
                RecordingsFilter.SCHEDULED_FILTER -> {
                    allRecordings.forEach {
                        if (it.status == RecordingStatus.SCHEDULED) filteredRecordings.add(it)
                    }
                    if (filteredRecordings.isEmpty()) showRecordings.setText(R.string.show_empty_scheduled_recordings)
                    else showRecordings.text = getQuantityString(R.plurals.show_scheduled_recording, filteredRecordings.size)
                }
            }
            showRecordings.visible()
        }
    }
}