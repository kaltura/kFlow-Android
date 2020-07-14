package com.kaltura.kflow.presentation.vod

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.AssetOrderBy
import com.kaltura.client.services.AssetService
import com.kaltura.client.types.Asset
import com.kaltura.client.types.FilterPager
import com.kaltura.client.types.SearchAssetFilter
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 2020-01-16.
 */
class GetVodViewModel(private val apiManager: PhoenixApiManager,
                      private val preferenceManager: PreferenceManager) : BaseViewModel(apiManager) {

    val getAssetList = MutableLiveData<Resource<ArrayList<Asset>>>()

    fun getVodAssetList(name: String, assetType: String) {
        val assetTypes = assetType.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        val filter = SearchAssetFilter().apply {
            orderBy = AssetOrderBy.START_DATE_DESC.value
            kSql = "(or name~\'$name\'"
            assetTypes.forEach {
                kSql += " asset_type=\'$it\'"
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

                preferenceManager.vodAssetType = assetType
            } else getAssetList.value = Resource.Error(it.error)
        })
    }

    fun getVodAssetType() = preferenceManager.vodAssetType
}