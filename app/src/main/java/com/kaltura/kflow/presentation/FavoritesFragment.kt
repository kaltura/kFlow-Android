package com.kaltura.kflow.presentation

import android.os.Bundle
import android.view.View
import com.kaltura.client.services.FavoriteService
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.getQuantityString
import com.kaltura.kflow.presentation.extension.gone
import com.kaltura.kflow.presentation.extension.visible
import com.kaltura.kflow.presentation.extension.withInternetConnection
import com.kaltura.kflow.presentation.main.MainActivity
import com.kaltura.kflow.utils.Utils
import kotlinx.android.synthetic.main.fragment_favorites.*

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
class FavoritesFragment : DebugFragment(R.layout.fragment_favorites) {

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Favorites"

        getFavorites.setOnClickListener {
            Utils.hideKeyboard(view)
            getFavoritesRequest()
        }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        Utils.hideKeyboard(view)
        PhoenixApiManager.cancelAll()
    }
}