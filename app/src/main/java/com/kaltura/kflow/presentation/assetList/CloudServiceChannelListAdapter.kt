package com.kaltura.kflow.presentation.assetList

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.kflow.R
import com.kaltura.kflow.entity.ChannelCS
import com.kaltura.kflow.presentation.extension.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.channel_cs.*

class CloudServiceChannelListAdapter(private val isShowActions: Boolean) : RecyclerView.Adapter<CloudServiceChannelListAdapter.CSViewHolder>() {

    var channelassets: Array<ChannelCS> = arrayOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    inner class CSViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(asset: ChannelCS, position: Int = -1) {
            when {
                asset is ChannelCS -> {
                    channelName.text = asset.name
                    channelId.text = asset.id
                    channelDescription.text = if (asset.descripion.isNullOrEmpty()) "" else asset.descripion
                    channelLcn.text = asset.lcn.toString()
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CSViewHolder(parent.inflate(
        R.layout.channel_cs))

    override fun onBindViewHolder(
        holder: CloudServiceChannelListAdapter.CSViewHolder,
        position: Int
    ) {
        holder.bind(channelassets[position], position)
    }

    override fun getItemCount(): Int {
        return channelassets.size
    }
}