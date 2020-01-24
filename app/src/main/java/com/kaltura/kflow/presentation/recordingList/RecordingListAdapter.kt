package com.kaltura.kflow.presentation.recordingList

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.client.types.Recording
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_recording.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class RecordingListAdapter(private val recordings: ArrayList<Recording>) : RecyclerView.Adapter<RecordingListAdapter.MyViewHolder>() {

    private val TIME_FORMAT = SimpleDateFormat("d MMM, HH:mm", Locale.US)
    var recordingClickListener: (recording: Recording) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(parent.inflate(R.layout.item_recording))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(recordings[position])

    override fun getItemCount() = recordings.size

    inner class MyViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(recording: Recording) {
            recordingStatus.text = recording.status.value
            recordingId.text = "Asset ID: ${recording.assetId}"
            recordingContainer.setOnClickListener { recordingClickListener(recording) }

            val createDateCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            createDateCalendar.timeInMillis = recording.createDate * 1000
            val viewableUntilDateCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            viewableUntilDateCalendar.timeInMillis = recording.viewableUntilDate * 1000

            createDate.text = "Create date: ${TIME_FORMAT.format(createDateCalendar.time)}"
            viewableUntilDate.text = "Viewable until date: ${TIME_FORMAT.format(viewableUntilDateCalendar.time)}"
        }
    }
}