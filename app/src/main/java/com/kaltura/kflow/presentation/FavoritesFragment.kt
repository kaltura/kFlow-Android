package com.kaltura.kflow.presentation

import android.os.Bundle
import android.view.View
import com.kaltura.client.services.FavoriteService
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.synthetic.main.fragment_favorites.*

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
class FavoritesFragment : DebugFragment(R.layout.fragment_favorites) {

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getFavorites.setOnClickListener {
            hideKeyboard()
            getFavoritesRequest()
        }
    }

    override fun subscribeUI() {}

    private fun getFavoritesRequest() {
        withInternetConnection {
            favoriteCount.gone()
            clearDebugView()
            PhoenixApiManager.execute(FavoriteService.list().setCompletion {
                if (it.isSuccess) {
                    favoriteCount.text = getQuantityString(R.plurals.favorite_count, it.results.totalCount)
                    favoriteCount.visible()
                }
            })
        }
    }
}