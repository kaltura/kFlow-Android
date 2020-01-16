package com.kaltura.kflow.presentation.favorites

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.services.FavoriteService
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 2020-01-16.
 */
class FavoritesViewModel : BaseViewModel() {

    val favoritesCount = MutableLiveData<Resource<Int>>()

    fun getFavorites() {
        PhoenixApiManager.execute(FavoriteService.list().setCompletion {
            if (it.isSuccess) favoritesCount.value = Resource.Success(it.results.totalCount)
        })
    }
}