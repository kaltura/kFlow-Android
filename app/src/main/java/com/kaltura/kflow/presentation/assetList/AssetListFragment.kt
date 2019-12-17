package com.kaltura.kflow.presentation.assetList

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.types.Asset
import com.kaltura.client.types.ProgramAsset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.instanceOf
import com.kaltura.kflow.presentation.extension.replaceFragment
import com.kaltura.kflow.presentation.main.MainActivity
import com.kaltura.kflow.presentation.player.PlayerFragment
import com.kaltura.kflow.utils.Utils
import kotlinx.android.synthetic.main.fragment_vod_list.*
import java.util.*

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class AssetListFragment : Fragment(R.layout.fragment_vod_list) {

    companion object {
        const val ARG_ASSETS = "extra_assets"
        const val ARG_SCROLL_TO_LIVE = "extra_scroll_to_live"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Asset list"
        initList()
    }

    private fun initList() {
        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))

        val assets: ArrayList<Asset> = arguments?.getSerializable(ARG_ASSETS) as ArrayList<Asset>?
                ?: arrayListOf()
        val scrollToLive = arguments?.getBoolean(ARG_SCROLL_TO_LIVE) ?: false
        list.adapter = AssetListAdapter(assets).apply {
            vodClickListener = {
                replaceFragment(instanceOf<PlayerFragment>(PlayerFragment.ARG_ASSET to it), addToBackStack = true)
            }
            programClickListener = { asset, contextType ->
                replaceFragment(instanceOf<PlayerFragment>(PlayerFragment.ARG_ASSET to asset,
                        PlayerFragment.ARG_PLAYBACK_CONTEXT_TYPE to contextType), addToBackStack = true)
            }
        }
        if (scrollToLive && assets.isNotEmpty()) {
            var liveAssetPosition = assets.indexOfFirst { it is ProgramAsset && Utils.isProgramInLive(it) }
            if (liveAssetPosition < 0) liveAssetPosition = 0
            if (liveAssetPosition > 2) liveAssetPosition -= 3 // minus 3 items from the top, to move live asset to the middle of the screen
            list.scrollToPosition(liveAssetPosition)
        }
    }
}