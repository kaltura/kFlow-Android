package com.kaltura.kflow.presentation.main

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.dip
import com.kaltura.kflow.presentation.extension.inflate
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_feature.*
import kotlin.random.Random


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
                    card.background = getBackgroundGradient(card.context,
                            ContextCompat.getColor(card.context, R.color.yellow), ContextCompat.getColor(card.context, R.color.orange))
                    image.setImageResource(R.drawable.ic_login)
                }
                Feature.ANONYMOUS_LOGIN -> {
                    card.background = getBackgroundGradient(card.context,
                            ContextCompat.getColor(card.context, R.color.blue))
                    image.setImageResource(R.drawable.ic_anonymous)
                }
                Feature.REGISTRATION -> {
                    card.background = getBackgroundGradient(card.context)
                    image.setImageResource(R.drawable.ic_registration)
                }
                Feature.SEARCH -> {
                    card.background = getBackgroundGradient(card.context)
                    image.setImageResource(R.drawable.ic_search)
                }
                Feature.FAVORITES -> {
                    card.background = getBackgroundGradient(card.context,
                            ContextCompat.getColor(card.context, R.color.yellow), ContextCompat.getColor(card.context, R.color.orange))
                    image.setImageResource(R.drawable.ic_favorite)
                }
                Feature.VOD -> {
                    card.background = getBackgroundGradient(card.context)
                }
                Feature.EPG -> {
                    card.background = getBackgroundGradient(card.context)
                }
                Feature.LIVE -> {
                    card.background = getBackgroundGradient(card.context)
                }
                Feature.MEDIA_PAGE -> {
                    card.background = getBackgroundGradient(card.context)
                }
                Feature.KEEP_ALIVE -> {
                    card.background = getBackgroundGradient(card.context)
                }
                Feature.SUBSCRIPTION -> {
                    card.background = getBackgroundGradient(card.context)
                }
                Feature.PRODUCT_PRICE -> {
                    card.background = getBackgroundGradient(card.context)
                }
                Feature.CHECK_RECEIPT -> {
                    card.background = getBackgroundGradient(card.context)
                }
                Feature.TRANSACTION_HISTORY -> {
                    card.background = getBackgroundGradient(card.context)
                    image.setImageResource(R.drawable.ic_transaction_history)
                }
                Feature.RECORDINGS -> {
                    card.background = getBackgroundGradient(card.context)
                }
                Feature.SETTINGS -> {
                    card.background = getBackgroundGradient(card.context,
                            ContextCompat.getColor(card.context, R.color.yellow), ContextCompat.getColor(card.context, R.color.red))
                    image.setImageResource(R.drawable.ic_settings)
                }
            }
        }

        private fun getBackgroundGradient(context: Context, vararg exceptColors: Int): GradientDrawable {
            var colors = context.resources.getIntArray(R.array.colors)
            if (exceptColors.isNotEmpty())
                colors = colors.filterNot { exceptColors.contains(it) }.toIntArray()

            return GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    intArrayOf(colors[Random.nextInt(colors.size)], colors[Random.nextInt(colors.size)])).apply {
                cornerRadius = context.dip(8).toFloat()
            }
        }
    }
}