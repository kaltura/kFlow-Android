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

    var clickListener: (feature: Feature) -> Unit = { }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(parent.inflate(R.layout.item_feature))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(features[position])

    override fun getItemCount() = features.size

    inner class MyViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(feature: Feature) {
            featureText.text = feature.text
            card.setOnClickListener { clickListener(feature) }
            when (feature) {
                Feature.LOGIN -> {
                    image.setImageResource(R.drawable.ic_login)
                }
                Feature.ANONYMOUS_LOGIN -> {
                    image.setImageResource(R.drawable.ic_anonymous)
                }
                Feature.REGISTRATION -> {
                    image.setImageResource(R.drawable.ic_registration)
                }
                Feature.SEARCH -> {
                    image.setImageResource(R.drawable.ic_search)
                }
                Feature.FAVORITES -> {
                    image.setImageResource(R.drawable.ic_favorite)
                }
                Feature.VOD -> {
                }
                Feature.EPG -> {
                }
                Feature.LIVE -> {
                }
                Feature.MEDIA_PAGE -> {
                }
                Feature.KEEP_ALIVE -> {
                }
                Feature.SUBSCRIPTION -> {
                }
                Feature.PRODUCT_PRICE -> {
                }
                Feature.CHECK_RECEIPT -> {
                }
                Feature.TRANSACTION_HISTORY -> {
                    image.setImageResource(R.drawable.ic_transaction_history)
                }
                Feature.RECORDINGS -> {
                }
                Feature.SETTINGS -> {
                    image.setImageResource(R.drawable.ic_settings)
                }
            }
        }
    }
}