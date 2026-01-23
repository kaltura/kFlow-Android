package com.kaltura.kflow.presentation.assetList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.types.Asset
import com.kaltura.client.types.ProgramAsset
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentAnonymousLoginBinding
import com.kaltura.kflow.databinding.FragmentVodListBinding
import com.kaltura.kflow.presentation.base.BaseFragment
import com.kaltura.kflow.presentation.extension.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
class AssetListFragment : BaseFragment(R.layout.fragment_vod_list) {

    private val viewModel: AssetListViewModel by viewModel()
    private val args: AssetListFragmentArgs by navArgs()
    private val adapter by lazy {
        AssetListAdapter(args.isShowActions).apply {
            vodClickListener = { asset ->
                val startPosition = args.watchedAssets?.firstOrNull { it.asset.id == asset.id }?.position
                        ?: 0
                navigate(AssetListFragmentDirections.navigateToPlayer(asset = asset, startPosition = startPosition))
            }
            programClickListener = { asset, contextType ->
                navigate(AssetListFragmentDirections.navigateToPlayer(asset = asset, playbackContextType = contextType.value))
            }
            reminderClickListener = { asset ->

                setAddReminderRequest(asset)
            }
        }
    }
    private var _binding: FragmentVodListBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVodListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        initList()
    }

    private fun initList() {
        binding.list.setHasFixedSize(true)
        binding.list.layoutManager = LinearLayoutManager(requireContext())
        binding.list.layoutAnimation =
                if (adapter.assets.isEmpty()) AnimationUtils.loadLayoutAnimation(context, R.anim.item_layout_animation)
                else null

        binding.list.adapter = adapter
        if (args.watchedAssets == null) adapter.assets = args.assets!!
        else adapter.watchedAssets = args.watchedAssets!!

        if (args.isScrollToLive && args.assets!!.isNotEmpty()) {
            var liveAssetPosition = args.assets!!.indexOfFirst { it is ProgramAsset && it.isProgramInLive() }
            if (liveAssetPosition < 0) liveAssetPosition = 0
            if (liveAssetPosition > 2) liveAssetPosition -= 3 // minus 3 items from the top, to move live asset to the middle of the screen
            binding.list.scrollToPosition(liveAssetPosition)
        }
    }

    private fun setAddReminderRequest(program: Asset) {
        withInternetConnection {
            viewModel.makeAddReminderRequest(program)
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.reminderAddingEvent,
            error = {
                it.printStackTrace()
                longToast("Adding Reminder Error : $it")
            },
            success = {
                longToast("The following Reminder was successfully added : "+it)
            })
    }
}