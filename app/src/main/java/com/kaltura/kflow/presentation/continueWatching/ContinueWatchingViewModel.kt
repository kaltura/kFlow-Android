package com.kaltura.kflow.presentation.continueWatching

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.WatchStatus
import com.kaltura.client.services.AssetHistoryService
import com.kaltura.client.services.AssetService
import com.kaltura.client.types.*
import com.kaltura.kflow.entity.WatchedAsset
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 2020-01-16.
 */
class ContinueWatchingViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val getWatchedAssetList = MutableLiveData<Resource<ArrayList<WatchedAsset>>>()

    fun getWatchedAssets() {
        val filter = AssetHistoryFilter().apply {
            statusEqual = WatchStatus.PROGRESS
            daysLessThanOrEqual = 5
        }

        val filterPager = FilterPager().apply {
            pageIndex = 1
            pageSize = 50
        }

        apiManager.execute(AssetHistoryService.list(filter, filterPager).setCompletion {
            if (it.isSuccess) getAssetsByIds((it.results.objects as ArrayList<AssetHistory>))
            else getWatchedAssetList.value = Resource.Error(it.error)
        })
    }

    private fun getAssetsByIds(assetsHistory: List<AssetHistory>) {
        if (assetsHistory.isEmpty()) getWatchedAssetList.value = Resource.Success(arrayListOf())
        else {
            val filter = SearchAssetFilter().apply {
                kSql = "(or"
                assetsHistory.forEach {
                    kSql += " media_id=\'${it.assetId}\'"
                }
                kSql += ")"
            }

            val filterPager = FilterPager().apply {
                pageIndex = 1
                pageSize = 50
            }
            apiManager.execute(AssetService.list(filter, filterPager).setCompletion {
                if (it.isSuccess) {
                    if (it.results.objects != null) {
                        getWatchedAssetList.value =
                                Resource.Success(it.results.objects.map { asset ->
                                    WatchedAsset(asset,
                                            assetsHistory.firstOrNull {
                                                it.assetId == asset.id
                                            }?.position ?: 0)
                                } as ArrayList<WatchedAsset>)
                    } else getWatchedAssetList.value = Resource.Success(arrayListOf())
                } else getWatchedAssetList.value = Resource.Error(it.error)
            })
        }
    }
}