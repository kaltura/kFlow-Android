package com.kaltura.kflow.presentation.recordings

import androidx.lifecycle.MutableLiveData
//import com.kaltura.client.services.RecordingService
//import com.kaltura.client.types.Recording
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 17.01.2020.
 */
class RecordingsViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

//    val recordingList = MutableLiveData<Resource<ArrayList<Recording>>>()

    fun getRecordings() {
//        apiManager.execute(RecordingService.list().setCompletion {
//            if (it.isSuccess) recordingList.value = Resource.Success(it.results.objects as ArrayList<Recording>)
//        })
    }
}