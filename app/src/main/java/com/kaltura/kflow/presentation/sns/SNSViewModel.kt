package com.kaltura.kflow.presentation.sns
import android.util.Log
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.kaltura.client.services.NotificationService
import com.kaltura.client.services.NotificationsSettingsService
import com.kaltura.client.types.APIException
import com.kaltura.client.types.NotificationsSettings
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource
import com.kaltura.kflow.utils.SingleLiveEvent

class SNSViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val pushRegistrationEvent = SingleLiveEvent<Resource<Boolean>>()
    val getPushStatusEvent = SingleLiveEvent<Resource<Boolean>>()
    val setPushStatusEvent = SingleLiveEvent<Resource<Boolean>>()

    fun setDevicePushTokenRequest() {

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.d(
                        "Kflow",
                        "Fetching FCM registration token failed",
                        task.exception
                    )
                    pushRegistrationEvent.postValue(Resource.Error(task.exception as APIException))
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d("Kflow", "Push Token Received :" +token)
                apiManager.execute(NotificationService.setDevicePushToken(token).setCompletion {
                    if (it.isSuccess) {
                        pushRegistrationEvent.postValue(Resource.Success(it.results))
                    } else {
                        pushRegistrationEvent.postValue(Resource.Error(it.error))
                    }
                })
            })
    }

    fun getNotificationStatus() {

        apiManager.execute((NotificationsSettingsService.get()).setCompletion {
            if (it.isSuccess) {
                getPushStatusEvent.postValue(Resource.Success(it.results.pushNotificationEnabled))
            } else {
                getPushStatusEvent.postValue(Resource.Error(it.error))
            }
        })

    }

    fun setNotificationStatus(checked: Boolean) {

        var settings = NotificationsSettings()
        settings.pushNotificationEnabled = checked

        apiManager.execute((NotificationsSettingsService.update(settings)).setCompletion {
            if (it.isSuccess) {
                setPushStatusEvent.postValue(Resource.Success(it.results))
            } else {
                setPushStatusEvent.postValue(Resource.Error(it.error))
            }
        })

    }
}