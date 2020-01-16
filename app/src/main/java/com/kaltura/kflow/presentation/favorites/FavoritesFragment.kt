package com.kaltura.kflow.presentation.favorites

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.synthetic.main.fragment_favorites.*

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
class FavoritesFragment : DebugFragment(R.layout.fragment_favorites) {

    private val viewModel: FavoritesViewModel by viewModels()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getFavorites.setOnClickListener {
            hideKeyboard()
            getFavoritesRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.favoritesCount) {
            favoriteCount.text = getQuantityString(R.plurals.favorite_count, it)
            favoriteCount.visible()
        }
    }

    private fun getFavoritesRequest() {
        withInternetConnection {
            favoriteCount.gone()
            clearDebugView()
            viewModel.getFavorites()
        }
    }
}