package com.kaltura.kflow.presentation.collections

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentCollectionsBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class GetCollectionsFragment : SharedTransitionFragment(R.layout.fragment_collections) {

    private var assets = arrayListOf<Asset>()
    private val viewModel: GetCollectionsViewModel by viewModel()

    override val feature = Feature.COLLECTIONS
    private var _binding: FragmentCollectionsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentCollectionsBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.showAssets.navigateOnClick {
            GetCollectionsFragmentDirections.navigateToAssetList(assets = assets.toTypedArray(), isShowActions = false)
        }
        binding.get.setOnClickListener {
            hideKeyboard()
            makeGetCollectionsRequest(binding.collectionId.string)
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.getCollectionList,
                error = { binding.get.error(lifecycleScope) },
                success = {
                    binding.get.success(lifecycleScope)
                    assets = it
                    binding.showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                    binding.showAssets.visible()
                })
    }

    private fun makeGetCollectionsRequest(collectionId: String) {
        withInternetConnection {
            clearDebugView()
            binding.showAssets.gone()

            if (collectionId.isEmpty()) {
                binding.collectionIdInputLayout.showError("Empty collection ID")
                return@withInternetConnection
            }
            if (TextUtils.isDigitsOnly(collectionId).not()) {
                binding.collectionIdInputLayout.showError("Wrong input")
                return@withInternetConnection
            }

            binding.get.startAnimation {
                viewModel.getCollectionList(collectionId)
            }
        }
    }
}