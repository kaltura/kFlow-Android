package com.kaltura.kflow.presentation.recordings

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.services.RecordingService
import com.kaltura.client.types.FilterPager
import com.kaltura.client.types.Recording
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 17.01.2020.
 */
class RecordingsViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val recordingList = MutableLiveData<Resource<ArrayList<Recording>>>()

    fun getRecordings() {
        val filterPager = FilterPager().apply {
            pageIndex = 1
            pageSize = 100
        }
        apiManager.execute(RecordingService.list(null,filterPager).setCompletion() {
            if (it.isSuccess) {
                if (it.results.objects != null) recordingList.value = Resource.Success(it.results.objects as ArrayList<Recording>)
                else recordingList.value = Resource.Success(arrayListOf())
            } else recordingList.value = Resource.Error(it.error)
        })
    }
}