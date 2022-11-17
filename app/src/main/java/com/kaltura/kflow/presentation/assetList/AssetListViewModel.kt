package com.kaltura.kflow.presentation.assetList
import com.kaltura.client.enums.ReminderType
import com.kaltura.client.services.ReminderService
import com.kaltura.client.types.Asset
import com.kaltura.client.types.AssetReminder
import com.kaltura.client.types.Reminder
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource
import com.kaltura.kflow.utils.SingleLiveEvent

class AssetListViewModel (private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val reminderAddingEvent = SingleLiveEvent<Resource<Reminder>>()

    fun makeAddReminderRequest(asset: Asset) {

        var reminder = AssetReminder()
        reminder.assetId = asset.id
        reminder.type = ReminderType.ASSET

        apiManager.execute(
            ReminderService.AddReminderBuilder(reminder)
            .setCompletion {
                if (it.isSuccess) {
                    reminderAddingEvent.postValue(Resource.Success(it.results))
                } else {
                    reminderAddingEvent.postValue(Resource.Error(it.error))
                }
            })
    }

}