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
class SearchViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val assets = MutableLiveData<Resource<ArrayList<Asset>>>()
    val historyAssetsCount = MutableLiveData<Resource<Int>>()

    fun search(assetType: String, kSqlSearch: String) {
        val filter = SearchAssetFilter().apply {
            val assetTypes = assetType.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            kSql = "(or description ~ \'$kSqlSearch\' name ~ \'$kSqlSearch\'"
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
                if (it.results.objects != null) assets.value = Resource.Success(it.results.objects as ArrayList<Asset>)
                else assets.value = Resource.Success(arrayListOf())
            } else {
                assets.value = Resource.Error(it.error)
            }
        })
    }

    fun searchHistory() {
        val filter = SearchHistoryFilter().apply { orderBy = SearchHistoryOrderBy.NONE.value }
        val filterPager = FilterPager().apply {
            pageIndex = 1
            pageSize = 50
        }

        apiManager.execute(SearchHistoryService.list(filter, filterPager).setCompletion {
            if (it.isSuccess) historyAssetsCount.value = Resource.Success(it.results.totalCount)
            else historyAssetsCount.value = Resource.Error(it.error)
        })
    }
}