package com.kaltura.kflow.presentation.liveTv

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.AssetOrderBy
import com.kaltura.client.services.AssetService
import com.kaltura.client.types.Asset
import com.kaltura.client.types.FilterPager
import com.kaltura.client.types.SearchAssetFilter
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 17.01.2020.
 */
class LiveTvViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val channelList = MutableLiveData<Resource<ArrayList<Asset>>>()

    fun getChannels(channelName: String) {
        val filter = SearchAssetFilter().apply {
            orderBy = AssetOrderBy.START_DATE_DESC.value
            name = channelName
            kSql = "(and name~'$channelName' (and (and customer_type_blacklist != '5' (or region_agnostic_user_types = '5' (or region_whitelist = '1077' (and region_blacklist != '1077' (or region_whitelist !+ '' region_whitelist = '0'))))) asset_type='600'))"
        }

        val filterPager = FilterPager().apply {
            pageIndex = 1
            pageSize = 50
        }

        apiManager.execute(AssetService.list(filter, filterPager).setCompletion {
            if (it.isSuccess) {
                if (it.results.objects != null) channelList.value = Resource.Success(it.results.objects as ArrayList<Asset>)
                else channelList.value = Resource.Success(arrayListOf())
            } else channelList.value = Resource.Error(it.error)
        })
    }
}