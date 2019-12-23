package com.kaltura.kflow.presentation

import android.os.Bundle
import android.view.View
import com.kaltura.client.enums.RecordingStatus
import com.kaltura.client.services.RecordingService
import com.kaltura.client.types.Recording
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.MainActivity
import com.kaltura.kflow.presentation.recordingList.RecordingListFragment
import kotlinx.android.synthetic.main.fragment_media_page.debugView
import kotlinx.android.synthetic.main.fragment_recordings.*

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
class RecordingsFragment : DebugFragment(R.layout.fragment_recordings) {

    private val allRecordings = arrayListOf<Recording>()
    private val filteredRecordings = arrayListOf<Recording>()
    private var recordingFilter = RecordingsFilter.RECORDED_FILTER

    private enum class RecordingsFilter {
        RECORDED_FILTER,
        ON_GOING_FILTER,
        SCHEDULED_FILTER
    }

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Recordings"

        showRecordings.setOnClickListener {
            hideKeyboard()
            if (filteredRecordings.isNotEmpty()) {
                replaceFragment(instanceOf<RecordingListFragment>(RecordingListFragment.ARG_RECORDINGS to filteredRecordings), addToBackStack = true)
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
        showRecordings.visibleOrGone(allRecordings.isNotEmpty())
        filterRecordings()
    }

    private fun recordingsRequest() {
        if (allRecordings.isEmpty()) {
            withInternetConnection {
                allRecordings.clear()
                filteredRecordings.clear()
                showRecordings.gone()
                clearDebugView()
                PhoenixApiManager.execute(RecordingService.list().setCompletion {
                    if (it.isSuccess) {
                        allRecordings.addAll(it.results.objects)
                        filterRecordings()
                    }
                })
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

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        PhoenixApiManager.cancelAll()
    }
}