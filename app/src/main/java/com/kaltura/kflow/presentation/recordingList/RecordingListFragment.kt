package com.kaltura.kflow.presentation.recordingList

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.enums.RecordingStatus
import com.kaltura.client.types.Recording
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.navigate
import kotlinx.android.synthetic.main.fragment_recording_list.*
import java.util.*

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class RecordingListFragment : Fragment(R.layout.fragment_recording_list) {

    companion object {
        const val ARG_RECORDINGS = "extra_recordings"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
    }

    private fun initList() {
        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        val recordings = arguments?.getSerializable(ARG_RECORDINGS) as? ArrayList<Recording> ?: arrayListOf()
        list.adapter = RecordingListAdapter(recordings).apply {
            recordingClickListener = {
                if (it.status == RecordingStatus.RECORDED)
                    navigate(RecordingListFragmentDirections.navigateToPlayer(recording = it))
            }
        }
    }
}