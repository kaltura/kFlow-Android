package com.kaltura.kflow.presentation.recordingList

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.enums.RecordingStatus
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.BaseFragment
import com.kaltura.kflow.presentation.extension.navigate
import kotlinx.android.synthetic.main.fragment_recording_list.*
import kotlinx.android.synthetic.main.fragment_recording_list.list

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class RecordingListFragment : BaseFragment(R.layout.fragment_recording_list) {

    private val args: RecordingListFragmentArgs by navArgs()

    private val adapter = RecordingListAdapter().apply {
        recordingClickListener = {
            if (it.status == RecordingStatus.RECORDED)
                navigate(RecordingListFragmentDirections.navigateToPlayer(recording = it))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        initList()
    }

    override fun subscribeUI() {}

    private fun initList() {
        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.layoutAnimation =
                if (adapter.recordings.isEmpty()) AnimationUtils.loadLayoutAnimation(context, R.anim.item_layout_animation)
                else null
        list.adapter = adapter
        adapter.recordings = args.recordings
    }
}