package com.kaltura.kflow.presentation.vod

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.AssetOrderBy
import com.kaltura.client.services.AssetService
import com.kaltura.client.types.APIException
import com.kaltura.client.types.Asset
import com.kaltura.client.types.FilterPager
import com.kaltura.client.types.SearchAssetFilter
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 2020-01-16.
 */
class GetVodViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val getAssetList = MutableLiveData<Resource<ArrayList<Asset>>>()

    fun getVodAssetList(kSqlRequest: String) {
        val filter = SearchAssetFilter().apply {
            orderBy = AssetOrderBy.START_DATE_DESC.value
            kSql = kSqlRequest
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