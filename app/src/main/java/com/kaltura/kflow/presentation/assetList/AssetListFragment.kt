package com.kaltura.kflow.presentation.assetList

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.types.Asset
import com.kaltura.client.types.ProgramAsset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.BaseFragment
import com.kaltura.kflow.presentation.extension.isProgramInLive
import com.kaltura.kflow.presentation.extension.navigate
import com.kaltura.kflow.presentation.player.PlayerFragment
import kotlinx.android.synthetic.main.fragment_vod_list.*
import java.util.*

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class AssetListFragment : BaseFragment(R.layout.fragment_vod_list) {

    companion object {
        const val ARG_ASSETS = "extra_assets"
    }

    private val args: AssetListFragmentArgs by navArgs()
    private val adapter = AssetListAdapter().apply {
        vodClickListener = {
            navigate(AssetListFragmentDirections.navigateToPlayer(asset = it))
        }
        programClickListener = { asset, contextType ->
            navigate(AssetListFragmentDirections.navigateToPlayer(asset = asset), PlayerFragment.ARG_PLAYBACK_CONTEXT_TYPE to contextType)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        initList()
    }

    private fun initList() {
        val assets: ArrayList<Asset> = arguments?.getSerializable(ARG_ASSETS) as ArrayList<Asset>?
                ?: arrayListOf()

        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.layoutAnimation =
                if (adapter.assets.isEmpty()) AnimationUtils.loadLayoutAnimation(context, R.anim.item_layout_animation)
                else null

        list.adapter = adapter
        adapter.assets = assets
        if (args.isScrollToLive && assets.isNotEmpty()) {
            var liveAssetPosition = assets.indexOfFirst { it is ProgramAsset && it.isProgramInLive() }
            if (liveAssetPosition < 0) liveAssetPosition = 0
            if (liveAssetPosition > 2) liveAssetPosition -= 3 // minus 3 items from the top, to move live asset to the middle of the screen
            list.scrollToPosition(liveAssetPosition)
        }
    }

    override fun subscribeUI() {}
}