package com.kaltura.kflow.presentation.registration

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.services.OttUserService
import com.kaltura.client.types.OTTUser
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 17.01.2020.
 */
class RegistrationViewModel(private val apiManager: PhoenixApiManager,
                            private val preferenceManager: PreferenceManager) : BaseViewModel(apiManager) {

    val registerRequest = MutableLiveData<Resource<Unit>>()

    fun register(firstName: String, lastName: String, userName: String, email: String, password: String) {
        val ottUser = OTTUser().apply {
            firstName(firstName)
            lastName(lastName)
            username(userName)
            email(email)
        }

        apiManager.execute(OttUserService.register(preferenceManager.partnerId, ottUser, password)
                .setCompletion {
                    if (it.isSuccess) {
                        registerRequest.value = Resource.Success(Unit)
                    } else {
                        registerRequest.value = Resource.Error(it.error)
                    }
                })
    }
}