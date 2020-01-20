package com.kaltura.kflow.presentation.search

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.SearchHistoryOrderBy
import com.kaltura.client.services.AssetService
import com.kaltura.client.services.SearchHistoryService
import com.kaltura.client.types.Asset
import com.kaltura.client.types.FilterPager
import com.kaltura.client.types.SearchAssetFilter
import com.kaltura.client.types.SearchHistoryFilter
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 20.01.2020.
 */
class SearchViewModel : BaseViewModel() {

    val assets = MutableLiveData<Resource<ArrayList<Asset>>>()
    val historyAssetsCount = MutableLiveData<Resource<Int>>()

    fun search(typeInSearch: String, kSqlSearch: String) {
        val filter = SearchAssetFilter().apply {
            typeIn = typeInSearch
            kSql = "(or description ~ \'$kSqlSearch \' name ~ \'$kSqlSearch \')"
        }

        val filterPager = FilterPager().apply {
            pageIndex = 1
            pageSize = 50
        }

        PhoenixApiManager.execute(AssetService.list(filter, filterPager).setCompletion {
            if (it.isSuccess) {
                if (it.results.objects != null) assets.value = Resource.Success(it.results.objects as ArrayList<Asset>)
            }
        })
    }

    fun searchHistory() {
        val filter = SearchHistoryFilter().apply { orderBy = SearchHistoryOrderBy.NONE.value }
        val filterPager = FilterPager().apply {
            pageIndex = 1
            pageSize = 50
        }

        PhoenixApiManager.execute(SearchHistoryService.list(filter, filterPager).setCompletion {
            if (it.isSuccess) historyAssetsCount.value = Resource.Success(it.results.totalCount)
        })
    }
}