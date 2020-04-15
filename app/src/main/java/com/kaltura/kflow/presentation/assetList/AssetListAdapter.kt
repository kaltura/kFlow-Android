package com.kaltura.kflow.presentation.assetList

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.client.types.Asset
import com.kaltura.client.types.ProgramAsset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_asset.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class AssetListAdapter : RecyclerView.Adapter<AssetListAdapter.MyViewHolder>() {

    var vodClickListener: (asset: Asset) -> Unit = {}
    var programClickListener: (asset: Asset, contextType: APIDefines.PlaybackContextType) -> Unit = { _, _ -> Unit }

    var assets: ArrayList<Asset> = arrayListOf()
        set(value) {
            field.clear()
            field.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(parent.inflate(R.layout.item_asset))

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) = holder.bind(assets[position])

    override fun getItemCount() = assets.size

    inner class MyViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(asset: Asset) {
            if (asset is ProgramAsset) {
                val time = StringBuilder()
                val startFormat = SimpleDateFormat("d MMM, HH:mm", Locale.US)
                val endFormat = SimpleDateFormat("HH:mm", Locale.US)
                val startDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                startDayCalendar.timeInMillis = asset.getStartDate() * 1000
                val endDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                endDayCalendar.timeInMillis = asset.getEndDate() * 1000
                time.append(startFormat.format(startDayCalendar.time))
                        .append(" - ")
                        .append(endFormat.format(endDayCalendar.time))
                assetTime.text = time
                assetTime.visible()
            } else assetTime.gone()

            assetName.text = asset.name
            assetId.text = "Asset ID: ${asset.id}"
            if (asset is ProgramAsset && asset.isProgramInPast()) {
                playback.gone()
                startover.gone()
                catchUp.visible()
            } else if (asset is ProgramAsset && asset.isProgramInLive()) {
                playback.visible()
                startover.visible()
                catchUp.gone()
            } else if (asset is ProgramAsset && asset.isProgramInFuture()) {
                playback.gone()
                startover.gone()
                catchUp.gone()
            } else {
                playback.gone()
                startover.visible()
                catchUp.gone()
            }
            playback.setOnClickListener {
                if (asset is ProgramAsset) programClickListener(asset, APIDefines.PlaybackContextType.Playback)
                else vodClickListener(asset)
            }
            startover.setOnClickListener { programClickListener(asset, APIDefines.PlaybackContextType.StartOver) }
            catchUp.setOnClickListener { programClickListener(asset, APIDefines.PlaybackContextType.Catchup) }
        }
    }
}