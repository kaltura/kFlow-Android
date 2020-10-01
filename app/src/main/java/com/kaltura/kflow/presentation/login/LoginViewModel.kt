package com.kaltura.kflow.presentation.login

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.services.OttUserService
import com.kaltura.client.types.StringValue
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource
import com.kaltura.playkit.player.PKHttpClientManager

/**
 * Created by alex_lytvynenko on 2020-01-09.
 */
class LoginViewModel(private val apiManager: PhoenixApiManager,
                     private val preferenceManager: PreferenceManager) : BaseViewModel(apiManager) {

    val loginRequest = MutableLiveData<Resource<Unit>>()

    fun makeLoginRequest(email: String, password: String, udid: String, extraParams: HashMap<String, StringValue>? = null) {
        apiManager.execute(OttUserService.login(preferenceManager.partnerId, email, password, extraParams, udid)
                .setCompletion {
                    if (it.isSuccess) {
                        preferenceManager.ks = it.results.loginSession.ks
                        apiManager.ks = it.results.loginSession.ks
                        preferenceManager.authUser = email
                        preferenceManager.authPassword = password
                        loginRequest.value = Resource.Success(Unit)
                    } else {
                        loginRequest.value = Resource.Error(it.error)
                    }
                })
    }

    fun getSavedUsername() = preferenceManager.authUser

    fun getSavedPassword() = preferenceManager.authPassword
}