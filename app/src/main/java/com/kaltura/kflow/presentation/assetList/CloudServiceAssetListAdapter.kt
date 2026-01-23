package com.kaltura.kflow.presentation.assetList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.kflow.databinding.ItemCsBinding
import com.kaltura.kflow.entity.EPGProgram
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.extensions.LayoutContainer
import java.text.SimpleDateFormat
import java.util.*

class CloudServiceAssetListAdapter(private val isShowActions: Boolean) : RecyclerView.Adapter<CloudServiceAssetListAdapter.CSViewHolder>() {

    var epgassets: Array<EPGProgram> = arrayOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var _binding: ItemCsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CSViewHolder {
        val bindMe = ItemCsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CSViewHolder(bindMe,parent)
    }

    inner class CSViewHolder(val binding: ItemCsBinding,
                             override val containerView: View?
    ) : RecyclerView.ViewHolder(binding.root), LayoutContainer {

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
                    binding.assetDates.text = time
                    binding.assetDates.visible()
                }
                position >= 0 -> {
                    binding.assetDates.text = "Position: $position sec"
                    binding.assetDates.visible()
                }
                else -> {
                    binding.assetDates.gone()
                }
            }

            binding.assetName.text = asset.name
            binding.assetId.text = "Asset ID: ${asset.epgID}"
        }
    }

    override fun onBindViewHolder(holder: CloudServiceAssetListAdapter.CSViewHolder, position: Int) {
        holder.bind(epgassets[position], position)
    }

    override fun getItemCount(): Int {
        return epgassets.size
    }

}