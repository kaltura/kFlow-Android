package com.kaltura.kflow.presentation.reminderList

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.AssetOrderBy
import com.kaltura.client.services.ReminderService
import com.kaltura.client.types.*
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource
import com.kaltura.kflow.utils.SingleLiveEvent

class ReminderListViewModel (private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val deleteReminderEvent = SingleLiveEvent<Resource<Reminder>>()
    val reminderListEvent = MutableLiveData<Resource<ArrayList<Reminder>>>()


    fun makeGetRemindersRequest() {

        val filter = AssetReminderFilter().apply {
            orderBy = AssetOrderBy.START_DATE_DESC.value
        }

        apiManager.execute(
            ReminderService.list(filter)
                .setCompletion {
                    if (it.isSuccess) {
                        reminderListEvent.postValue(Resource.Success(it.results.objects as ArrayList<Reminder>))
                    } else {
                        reminderListEvent.postValue(Resource.Error(it.error))
                    }
                })
    }

    fun makeDeleteReminderRequest(reminder: Reminder) {

        apiManager.execute(
            ReminderService.delete(reminder.id.toLong(),reminder.type)
                .setCompletion {
                    if (it.isSuccess) {
                        deleteReminderEvent.postValue(Resource.Success(reminder))
                    } else {
                        deleteReminderEvent.postValue(Resource.Error(it.error))
                    }
                })
    }

}