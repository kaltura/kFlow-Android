package com.kaltura.kflow.presentation.favorites

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_favorites.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
class FavoritesFragment : SharedTransitionFragment(R.layout.fragment_favorites) {

    private val viewModel: FavoritesViewModel by viewModel()

    override fun debugView(): DebugView = debugView

    override val feature = Feature.FAVORITES

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFavorites.setOnClickListener {
            hideKeyboard()
            getFavoritesRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.favoritesCount,
                error = { getFavorites.error(lifecycleScope) },
                success = {
                    getFavorites.success(lifecycleScope)

                    favoriteCount.text = getQuantityString(R.plurals.favorite_count, it)
                    favoriteCount.visible()
                }
        )
    }

    private fun getFavoritesRequest() {
        withInternetConnection {
            favoriteCount.gone()
            clearDebugView()
            getFavorites.startAnimation {
                viewModel.getFavorites()
            }
        }
    }
}