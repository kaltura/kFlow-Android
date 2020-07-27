package com.kaltura.kflow.presentation.collections

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_collections.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class GetCollectionsFragment : SharedTransitionFragment(R.layout.fragment_collections) {

    private var assets = arrayListOf<Asset>()
    private val viewModel: GetCollectionsViewModel by viewModel()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.COLLECTIONS

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showAssets.navigateOnClick {
            GetCollectionsFragmentDirections.navigateToAssetList(assets = assets.toTypedArray(), isShowActions = false)
        }
        get.setOnClickListener {
            hideKeyboard()
            makeGetCollectionsRequest(collectionId.string)
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.getCollectionList,
                error = { get.error(lifecycleScope) },
                success = {
                    get.success(lifecycleScope)
                    assets = it
                    showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                    showAssets.visible()
                })
    }

    private fun makeGetCollectionsRequest(collectionId: String) {
        withInternetConnection {
            clearDebugView()
            showAssets.gone()

            if (collectionId.isEmpty()) {
                collectionIdInputLayout.showError("Empty collection ID")
                return@withInternetConnection
            }
            if (TextUtils.isDigitsOnly(collectionId).not()) {
                collectionIdInputLayout.showError("Wrong input")
                return@withInternetConnection
            }

            get.startAnimation {
                viewModel.getCollectionList(collectionId)
            }
        }
    }
}