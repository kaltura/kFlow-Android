package com.kaltura.kflow.presentation.assetList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.ChannelCsBinding
import com.kaltura.kflow.databinding.ItemCsBinding
import com.kaltura.kflow.entity.ChannelCS
import com.kaltura.kflow.presentation.extension.inflate
import kotlinx.android.extensions.LayoutContainer

class CloudServiceChannelListAdapter(private val isShowActions: Boolean) : RecyclerView.Adapter<CloudServiceChannelListAdapter.CSViewHolder>() {

    var channelassets: Array<ChannelCS> = arrayOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private var _binding: ChannelCsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : CloudServiceChannelListAdapter.CSViewHolder{

        val bindMe = ChannelCsBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return CSViewHolder(bindMe,parent)

    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CloudServiceAssetListAdapter.CSViewHolder {
//        val bindMe = ItemCsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return CSViewHolder(bindMe,parent)
//    }
    inner class CSViewHolder(val binding: ChannelCsBinding,override val containerView: View) : RecyclerView.ViewHolder(containerView),
        LayoutContainer {

        fun bind(asset: ChannelCS, position: Int = -1) {
            when {
                asset is ChannelCS -> {
                    binding.channelName.text = asset.name
                    binding.channelId.text = asset.id
                    binding.channelDescription.text = if (asset.descripion.isNullOrEmpty()) "" else asset.descripion
                    binding.channelLcn.text = asset.lcn.toString()
                }

            }
        }
    }

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