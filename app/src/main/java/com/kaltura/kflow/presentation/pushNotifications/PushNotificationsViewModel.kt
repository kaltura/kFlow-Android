package com.kaltura.kflow.presentation.pushNotifications

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.kaltura.client.services.NotificationService
import com.kaltura.client.types.APIException
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.*

/**
 * Created by alex_lytvynenko on 2020-01-14.
 */
class PushNotificationsViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val registrationEvent = SingleLiveEvent<Resource<Boolean>>()

    fun register() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                registrationEvent.value = Resource.Error(APIException(task.exception))
                return@OnCompleteListener
            }
            apiManager.execute(NotificationService.SetDevicePushTokenNotificationBuilder(task.result).setCompletion {
                if (it.isSuccess) registrationEvent.value = Resource.Success(it.results)
                else registrationEvent.value = Resource.Error(it.error)
            })
        })
    }
}