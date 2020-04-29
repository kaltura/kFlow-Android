package com.kaltura.kflow.presentation.recordingList

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.client.types.Recording
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_recording.*

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class RecordingListAdapter(private val recordings: Array<Recording>) : RecyclerView.Adapter<RecordingListAdapter.MyViewHolder>() {

    var recordingClickListener: (recording: Recording) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(parent.inflate(R.layout.item_recording))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(recordings[position])

    override fun getItemCount() = recordings.size

    inner class MyViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(recording: Recording) {
            recordingStatus.text = recording.status.value
            recordingId.text = "Asset ID: ${recording.assetId}"
            recordingContainer.setOnClickListener { recordingClickListener(recording) }
        }
    }
}