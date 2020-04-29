package com.kaltura.kflow.presentation.recordingList

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.enums.RecordingStatus
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.BaseFragment
import com.kaltura.kflow.presentation.extension.navigate
import kotlinx.android.synthetic.main.fragment_recording_list.*

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class RecordingListFragment : BaseFragment(R.layout.fragment_recording_list) {

    private val args: RecordingListFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initList()
    }

    override fun subscribeUI() {}

    private fun initList() {
        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        list.adapter = RecordingListAdapter(args.recordings).apply {
            recordingClickListener = {
                if (it.status == RecordingStatus.RECORDED)
                    navigate(RecordingListFragmentDirections.navigateToPlayer(recording = it))
            }
        }
    }
}