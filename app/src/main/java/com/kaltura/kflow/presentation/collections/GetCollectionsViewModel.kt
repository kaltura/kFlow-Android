package com.kaltura.kflow.presentation.collections

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.AssetOrderBy
import com.kaltura.client.services.AssetService
import com.kaltura.client.types.Asset
import com.kaltura.client.types.ChannelFilter
import com.kaltura.client.types.FilterPager
import com.kaltura.client.types.SearchAssetFilter
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 2020-01-16.
 */
class GetCollectionsViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val getCollectionList = MutableLiveData<Resource<ArrayList<Asset>>>()

    fun getCollectionList(collectionId: String) {
        val filter = ChannelFilter().apply {
            idEqual = collectionId.toInt()
        }

        val filterPager = FilterPager().apply {
            pageIndex = 1
            pageSize = 50
        }
        apiManager.execute(AssetService.list(filter, filterPager).setCompletion {
            if (it.isSuccess) {
                if (it.results.objects != null) getCollectionList.value = Resource.Success(it.results.objects as ArrayList<Asset>)
                else getCollectionList.value = Resource.Success(arrayListOf())
            } else getCollectionList.value = Resource.Error(it.error)
        })
    }
}