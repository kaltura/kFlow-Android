package com.kaltura.kflow.presentation.favorites

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.fragment_favorites.showAssets
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
class FavoritesFragment : SharedTransitionFragment(R.layout.fragment_favorites) {

    private val viewModel: FavoritesViewModel by viewModel()
    private var assets = arrayListOf<Asset>()

    override fun debugView(): DebugView = debugView

    override val feature = Feature.FAVORITES

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showAssets.navigateOnClick { FavoritesFragmentDirections.navigateToAssetList(assets = assets.toTypedArray()) }
        getFavorites.setOnClickListener {
            hideKeyboard()
            getFavoritesRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.getAssetList,
                error = { getFavorites.error(lifecycleScope) },
                success = {
                    getFavorites.success(lifecycleScope)
                    assets = it
                    showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                    showAssets.visible()
                }
        )
    }

    private fun getFavoritesRequest() {
        withInternetConnection {
            showAssets.gone()
            clearDebugView()
            getFavorites.startAnimation {
                viewModel.getFavorites()
            }
        }
    }
}