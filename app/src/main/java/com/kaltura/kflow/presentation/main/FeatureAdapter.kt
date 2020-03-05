package com.kaltura.kflow.presentation.main

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feature.*

/**
 * Created by alex_lytvynenko on 11/16/18.
 */
class FeatureAdapter(private val features: Array<Feature>) : RecyclerView.Adapter<FeatureAdapter.MyViewHolder>() {

    var clickListener: (feature: Feature, image: View, title: View) -> Unit = { _, _, _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(parent.inflate(R.layout.item_feature))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(features[position])

    override fun getItemCount() = features.size

    inner class MyViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(feature: Feature) {
            featureText.transitionName = "${feature.text}_title"
            image.transitionName = "${feature.text}_image"

            featureText.text = feature.text
            if (feature.imageResId != -1) image.setImageResource(feature.imageResId)
            card.setOnClickListener {
                clickListener(feature, image, featureText)
            }
        }
    }
}