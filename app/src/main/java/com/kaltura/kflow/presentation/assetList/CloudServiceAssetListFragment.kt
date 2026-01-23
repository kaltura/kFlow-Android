package com.kaltura.kflow.presentation.assetList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentAppTokenBinding
import com.kaltura.kflow.databinding.FragmentVodListBinding
import com.kaltura.kflow.presentation.base.BaseFragment

class CloudServiceAssetListFragment : BaseFragment(R.layout.fragment_vod_list) {
    private val args: CloudServiceAssetListFragmentArgs by navArgs()
    private val adapter by lazy {
        CloudServiceAssetListAdapter(args.isShowActions).apply {

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
            if (adapter.epgassets.isEmpty())
                AnimationUtils.loadLayoutAnimation(context, R.anim.item_layout_animation)
            else
                null
        adapter.epgassets = args.epgassets!!
        binding.list.adapter = adapter
    }

    override fun subscribeUI() {

    }
}