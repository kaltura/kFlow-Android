package com.kaltura.kflow.presentation.epg

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kaltura.client.enums.AssetOrderBy
import com.kaltura.client.services.AssetService
import com.kaltura.client.types.FilterPager
import com.kaltura.client.types.ProgramAsset
import com.kaltura.client.types.SearchAssetFilter
import com.kaltura.kflow.data.cache.ProgramAssetDao
import com.kaltura.kflow.data.entity.ProgramAssetMapper
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource
import com.kaltura.kflow.utils.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

/**
 * Created by alex_lytvynenko on 2020-01-13.
 */
class EpgViewModel(
    private val apiManager: PhoenixApiManager,
    private val programAssetDao: ProgramAssetDao,
    private val programAssetMapper: ProgramAssetMapper
) : BaseViewModel(apiManager) {

    val getAssetList = MutableLiveData<Resource<ArrayList<ProgramAsset>>>()
    val cloudFetchTimeMs = SingleLiveEvent<Long>()
    val databaseFetchTimeMs = SingleLiveEvent<Long>()
    val databaseSaveTimeMs = SingleLiveEvent<Long>()
    private val programs = arrayListOf<ProgramAsset>()
    private var cloudFetchStartTime = 0L
    private var dbSaveTotalTime = 0L

    fun cancel() {
        apiManager.cancelAll()
        programs.clear()
        cloudFetchStartTime = 0L
        dbSaveTotalTime = 0L
    }

    fun getChannelsCloud(itemPerPage: Int, dateFilter: EpgFragment.DateFilter) {
        if (cloudFetchStartTime == 0L) cloudFetchStartTime = System.currentTimeMillis()
//        var startDate = 0L
//        var endDate = 0L
//        val todayMidnightCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
//            this[Calendar.MILLISECOND] = 0
//            this[Calendar.SECOND] = 0
//            this[Calendar.MINUTE] = 0
//            this[Calendar.HOUR_OF_DAY] = 0
//        }
//
//        when (dateFilter) {
//            EpgFragment.DateFilter.YESTERDAY -> {
//                endDate = todayMidnightCalendar.timeInMillis / 1000
//                todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, -1)
//                startDate = todayMidnightCalendar.timeInMillis / 1000
//            }
//            EpgFragment.DateFilter.TODAY -> {
//                startDate = todayMidnightCalendar.timeInMillis / 1000
//                todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, 1)
//                endDate = todayMidnightCalendar.timeInMillis / 1000
//            }
//            EpgFragment.DateFilter.TOMORROW -> {
//                todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, 1)
//                startDate = todayMidnightCalendar.timeInMillis / 1000
//                todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, 1)
//                endDate = todayMidnightCalendar.timeInMillis / 1000
//            }
//        }
        val filter = SearchAssetFilter().apply {
            orderBy = AssetOrderBy.START_DATE_DESC.value
            kSql = "asset_type='epg'"
        }
        val filterPager = FilterPager().apply {
            pageSize = itemPerPage
            pageIndex = programs.size / itemPerPage + 1
        }

        apiManager.execute(AssetService.list(filter, filterPager)
            .setCompletion {
                if (it.isSuccess) {
                    it.results.objects?.let {
                        saveAssets(it as ArrayList<ProgramAsset>)
                        programs.addAll(it)
                    }
                    getAssetList.postValue(Resource.Success(programs))

                    if (it.results.totalCount > programs.size)
                        getChannelsCloud(itemPerPage, dateFilter)
                    else {
                        cloudFetchTimeMs.postValue(System.currentTimeMillis() - cloudFetchStartTime - dbSaveTotalTime)
                        databaseSaveTimeMs.postValue(dbSaveTotalTime)
                    }
                } else getAssetList.postValue(Resource.Error(it.error))
            })
    }

    fun getChannelsDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val dbFetchStartTime = System.currentTimeMillis()
            val programEntity = programAssetDao.getAll()
            programs.addAll(programAssetMapper.transform(programEntity))
            getAssetList.postValue(Resource.Success(programs))
            databaseFetchTimeMs.postValue(System.currentTimeMillis() - dbFetchStartTime)
        }
    }

    private fun saveAssets(programs: ArrayList<ProgramAsset>) {
        viewModelScope.launch(Dispatchers.IO) {
            val saveStartTime = System.currentTimeMillis()
            programAssetDao.insert(programAssetMapper.transformToEntity(programs))
            dbSaveTotalTime += System.currentTimeMillis() - saveStartTime
        }
    }
}