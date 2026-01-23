package com.kaltura.kflow.presentation.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.ChannelCsBinding
import com.kaltura.kflow.databinding.ItemCsBinding
import com.kaltura.kflow.databinding.ItemFeatureBinding
import com.kaltura.kflow.presentation.assetList.CloudServiceChannelListAdapter
import com.kaltura.kflow.presentation.extension.inflate
import kotlinx.android.extensions.LayoutContainer


/**
 * Created by alex_lytvynenko on 11/16/18.
 */
class FeatureAdapter(private val features: Array<Feature>) : RecyclerView.Adapter<FeatureAdapter.MyViewHolder>() {

    var clickListener: (feature: Feature, image: View, title: View) -> Unit = { _, _, _ -> }
//    private var _binding: ItemFeatureBinding? = null
//    private val binding get() = _binding!!
    //override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(parent.inflate(R.layout.item_feature))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : FeatureAdapter.MyViewHolder{

        val bindMe = ItemFeatureBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return MyViewHolder(bindMe)

    }
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : CloudServiceChannelListAdapter.CSViewHolder{
//
//        val bindMe = ChannelCsBinding.inflate(LayoutInflater.from(parent.context),parent, false)
//        return CSViewHolder(bindMe,parent)
//
//    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(features[position])

    override fun getItemCount() = features.size

    inner class MyViewHolder(private val binding:ItemFeatureBinding ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(feature: Feature) = with(binding){
            binding.featureText.transitionName = "${feature.text}_title"
            binding.image.transitionName = "${feature.text}_image"

            binding.featureText.text = feature.text
            if (feature.imageResId != -1) binding.image.setImageResource(feature.imageResId)
            binding.card.setOnClickListener {
                clickListener(feature, binding.image, binding.featureText)
            }
        }
    }
}