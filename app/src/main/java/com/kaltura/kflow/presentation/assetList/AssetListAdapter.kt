package com.kaltura.kflow.presentation.assetList

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.client.types.Asset
import com.kaltura.client.types.ProgramAsset
import com.kaltura.kflow.databinding.ItemAssetBinding
import com.kaltura.kflow.entity.WatchedAsset
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import kotlinx.android.extensions.LayoutContainer
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class AssetListAdapter(private val isShowActions: Boolean) : RecyclerView.Adapter<AssetListAdapter.MyViewHolder>() {

    var vodClickListener: (asset: Asset) -> Unit = {}
    var programClickListener: (asset: Asset, contextType: APIDefines.PlaybackContextType) -> Unit = { _, _ -> Unit }
    var reminderClickListener: (asset: Asset) -> Unit = {}

    var assets: Array<Asset> = arrayOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var watchedAssets: Array<WatchedAsset> = arrayOf()
        set(value) {
            field = value
            assets = value.map { it.asset }.toTypedArray()
        }

    //override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MyViewHolder(parent.inflate(R.layout.item_asset))
    private var _binding: ItemAssetBinding? = null
    private val binding get() = _binding!!
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val bindMe = ItemAssetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(bindMe,parent)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (watchedAssets.isNotEmpty()) holder.bind(assets[position], watchedAssets[position].position)
        else holder.bind(assets[position])
    }

    override fun getItemCount() = assets.size

    inner class MyViewHolder(val binding: ItemAssetBinding,
                             override val containerView: View?
    ) : RecyclerView.ViewHolder(binding.root), LayoutContainer {

        fun bind(asset: Asset, position: Int = -1) {
            when {
                asset is ProgramAsset -> {
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
                    binding.assetTime.text = time
                    binding.assetTime.visible()
                }
                position >= 0 -> {
                    binding.assetTime.text = "Position: $position sec"
                    binding.assetTime.visible()
                }
                else -> {
                    binding.assetTime.gone()
                }
            }

            binding.assetName.text = asset.name
            binding.assetId.text = "Asset ID: ${asset.id}"
            if (asset is ProgramAsset && asset.isProgramInPast()) {
                binding.playback.gone()
                binding.startover.gone()
                binding.catchUp.visible()
                binding.reminder.gone()
            } else if (asset is ProgramAsset && asset.isProgramInLive()) {
                binding.playback.visible()
                binding.startover.visible()
                binding.catchUp.gone()
                binding.reminder.gone()
            } else if (asset is ProgramAsset && asset.isProgramInFuture()) {
                binding.playback.gone()
                binding.startover.gone()
                binding.catchUp.gone()
                binding.reminder.visible()
            } else {
                binding.playback.visible()
                binding.startover.gone()
                binding.catchUp.gone()
                binding.reminder.gone()
            }
            binding.playback.setOnClickListener {
                if (asset is ProgramAsset) programClickListener(asset, APIDefines.PlaybackContextType.Playback)
                else vodClickListener(asset)
            }
            binding.startover.setOnClickListener { programClickListener(asset, APIDefines.PlaybackContextType.StartOver) }
            binding.catchUp.setOnClickListener { programClickListener(asset, APIDefines.PlaybackContextType.Catchup) }

            binding.reminder.setOnClickListener{ reminderClickListener(asset) }

            if (isShowActions.not()) {
                binding.playback.gone()
                binding.startover.gone()
                binding.catchUp.gone()
                binding.reminder.gone()
            }
        }
    }
}