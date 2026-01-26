package com.kaltura.kflow.presentation.recordingList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.enums.RecordingStatus
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentRecordingListBinding
import com.kaltura.kflow.presentation.base.BaseFragment
import com.kaltura.kflow.presentation.extension.navigate
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
    private var _binding: FragmentRecordingListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecordingListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        initList()
    }

    override fun subscribeUI() {}

    private fun initList() {
        binding.list.setHasFixedSize(true)
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.layoutAnimation =
                if (adapter.recordings.isEmpty()) AnimationUtils.loadLayoutAnimation(context, R.anim.item_layout_animation)
                else null
        binding.list.adapter = adapter
        adapter.recordings = args.recordings
    }
}