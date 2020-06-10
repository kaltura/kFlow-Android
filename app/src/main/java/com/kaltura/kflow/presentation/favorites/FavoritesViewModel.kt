package com.kaltura.kflow.presentation.favorites

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.services.AssetService
import com.kaltura.client.services.FavoriteService
import com.kaltura.client.types.*
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 2020-01-16.
 */
class FavoritesViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val getAssetList = MutableLiveData<Resource<ArrayList<Asset>>>()

    fun getFavorites() {
        apiManager.execute(FavoriteService.list().setCompletion {
            if (it.isSuccess) getAssetsByIds((it.results.objects as ArrayList<Favorite>).map { it.assetId })
            else getAssetList.value = Resource.Error(it.error)
        })
    }

    private fun getAssetsByIds(ids: List<Long>) {
        if (ids.isEmpty()) getAssetList.value = Resource.Success(arrayListOf())
        else {
            val filter = SearchAssetFilter().apply {
                kSql = "(or"
                ids.forEach {
                    kSql += " media_id=\'$it\'"
                }
                kSql += ")"
            }

            val filterPager = FilterPager().apply {
                pageIndex = 1
                pageSize = 50
            }
            apiManager.execute(AssetService.list(filter, filterPager).setCompletion {
                if (it.isSuccess) {
                    if (it.results.objects != null) getAssetList.value = Resource.Success(it.results.objects as ArrayList<Asset>)
                    else getAssetList.value = Resource.Success(arrayListOf())
                } else getAssetList.value = Resource.Error(it.error)
            })
        }
    }
}