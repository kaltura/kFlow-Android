package com.kaltura.kflow.presentation.epg

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.AssetOrderBy
import com.kaltura.client.services.AssetService
import com.kaltura.client.types.Asset
import com.kaltura.client.types.FilterPager
import com.kaltura.client.types.SearchAssetFilter
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by alex_lytvynenko on 2020-01-13.
 */
class EpgViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val getAssetList = MutableLiveData<Resource<ArrayList<Asset>>>()

    fun getChannelsRequest(epgChannelId: String, dateFilter: EpgFragment.DateFilter) {
        var startDate = 0L
        var endDate = 0L
        val todayMidnightCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            this[Calendar.MILLISECOND] = 0
            this[Calendar.SECOND] = 0
            this[Calendar.MINUTE] = 0
            this[Calendar.HOUR_OF_DAY] = 0
        }

        when (dateFilter) {
            EpgFragment.DateFilter.YESTERDAY -> {
                endDate = todayMidnightCalendar.timeInMillis / 1000
                todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, -1)
                startDate = todayMidnightCalendar.timeInMillis / 1000
            }
            EpgFragment.DateFilter.TODAY -> {
                startDate = todayMidnightCalendar.timeInMillis / 1000
                todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, 1)
                endDate = todayMidnightCalendar.timeInMillis / 1000
            }
            EpgFragment.DateFilter.TOMORROW -> {
                todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, 1)
                startDate = todayMidnightCalendar.timeInMillis / 1000
                todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, 1)
                endDate = todayMidnightCalendar.timeInMillis / 1000
            }
        }
        val filter = SearchAssetFilter().apply {
            orderBy = AssetOrderBy.START_DATE_DESC.value
            kSql = "(and linear_media_id = '$epgChannelId' (and start_date > '$startDate' end_date < '$endDate'))"
        }
        val filterPager = FilterPager().apply {
            pageSize = 100
            pageIndex = 1
        }

        apiManager.execute(AssetService.list(filter, filterPager)
                .setCompletion {
                    if (it.isSuccess) {
                        if (it.results.objects != null) getAssetList.value = Resource.Success(it.results.objects as ArrayList<Asset>)
                        else getAssetList.value = Resource.Success(arrayListOf())
                    } else getAssetList.value = Resource.Error(it.error)
                })
    }
}