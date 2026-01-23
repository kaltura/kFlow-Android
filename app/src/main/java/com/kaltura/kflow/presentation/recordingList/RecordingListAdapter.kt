package com.kaltura.kflow.presentation.recordingList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.client.types.Recording
import com.kaltura.kflow.databinding.ItemRecordingBinding
import kotlinx.android.extensions.LayoutContainer

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class RecordingListAdapter : RecyclerView.Adapter<RecordingListAdapter.MyViewHolder>() {

    var recordingClickListener: (recording: Recording) -> Unit = {}

    var recordings: Array<Recording> = arrayOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val bindMe = ItemRecordingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(bindMe,parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(recordings[position])

    override fun getItemCount() = recordings.size
    private var _binding: ItemRecordingBinding? = null
    private val binding get() = _binding!!
    inner class MyViewHolder(val binding: ItemRecordingBinding,override val containerView: View) : RecyclerView.ViewHolder(binding.root), LayoutContainer {

        fun bind(recording: Recording) {
            binding.recordingStatus.text = recording.status.value
            binding.recordingId.text = "Asset ID: ${recording.assetId}"
            binding.recordingContainer.setOnClickListener { recordingClickListener(recording) }
        }
    }

//    inner class MyViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
//
//        fun bind(recording: Recording) {
//            binding.recordingStatus.text = recording.status.value
//            binding.recordingId.text = "Asset ID: ${recording.assetId}"
//            binding.recordingContainer.setOnClickListener { recordingClickListener(recording) }
//        }
//    }
}