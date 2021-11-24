package com.kaltura.kflow.presentation.assetList

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_vod_list.*

class CloudServiceAssetListFragment : BaseFragment(R.layout.fragment_vod_list) {
    private val args: CloudServiceAssetListFragmentArgs by navArgs()
    private val adapter by lazy {
        CloudServiceAssetListAdapter(args.isShowActions).apply {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        initList()
    }

    private fun initList() {
        list.setHasFixedSize(true)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.layoutAnimation =
            if (adapter.epgassets.isEmpty())
                AnimationUtils.loadLayoutAnimation(context, R.anim.item_layout_animation)
            else
                null
        adapter.epgassets = args.epgassets!!
        list.adapter = adapter
    }

    override fun subscribeUI() {

    }
}