package com.kaltura.kflow.presentation.assetList

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.kflow.R
import com.kaltura.kflow.entity.EPGProgram
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_cs.*
import java.text.SimpleDateFormat
import java.util.*

class CloudServiceAssetListAdapter(private val isShowActions: Boolean) : RecyclerView.Adapter<CloudServiceAssetListAdapter.CSViewHolder>() {

    var epgassets: Array<EPGProgram> = arrayOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CSViewHolder(parent.inflate(
        R.layout.item_cs))


    inner class CSViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(asset: EPGProgram, position: Int = -1) {
            when {
                asset is EPGProgram -> {
                    val time = StringBuilder()
                    val startFormat = SimpleDateFormat("d MMM, HH:mm", Locale.US)
                    val endFormat = SimpleDateFormat("HH:mm", Locale.US)
                    val startDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    startDayCalendar.timeInMillis = asset.startDate * 1000
                    val endDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    endDayCalendar.timeInMillis = asset.endDate * 1000
                    time.append(startFormat.format(startDayCalendar.time))
                        .append(" - ")
                        .append(endFormat.format(endDayCalendar.time))
                    assetDates.text = time
                    assetDates.visible()
                }
                position >= 0 -> {
                    assetDates.text = "Position: $position sec"
                    assetDates.visible()
                }
                else -> {
                    assetDates.gone()
                }
            }

            assetName.text = asset.name
            assetId.text = "Asset ID: ${asset.epgID}"
        }
    }

    override fun onBindViewHolder(holder: CloudServiceAssetListAdapter.CSViewHolder, position: Int) {
        holder.bind(epgassets[position], position)
    }

    override fun getItemCount(): Int {
        return epgassets.size
    }

}