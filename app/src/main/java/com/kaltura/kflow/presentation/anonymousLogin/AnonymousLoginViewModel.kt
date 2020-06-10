package com.kaltura.kflow.presentation.anonymousLogin

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.AppTokenHashType
import com.kaltura.client.services.AppTokenService
import com.kaltura.client.services.OttUserService
import com.kaltura.client.types.AppToken
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 2020-01-15.
 */
class AnonymousLoginViewModel(private val apiManager: PhoenixApiManager,
                              private val preferenceManager: PreferenceManager) : BaseViewModel(apiManager) {

    val loginRequest = MutableLiveData<Resource<Unit>>()

    fun anonymousLogin(udid: String) {
        apiManager.execute(OttUserService.anonymousLogin(preferenceManager.partnerId, udid)
                .setCompletion {
                    if (it.isSuccess) {
                        preferenceManager.ks = it.results.ks
                        apiManager.ks = it.results.ks
                        loginRequest.value = Resource.Success(Unit)
                    } else {
                        loginRequest.value = Resource.Error(it.error)
                    }
                })
    }

    fun generateAppToken() {
        val appToken = AppToken()
        appToken.hashType = AppTokenHashType.SHA256
        appToken.sessionDuration = 604800 // 604800 seconds = 7 days
        appToken.expiry = 1832668157
        val requestBuilder = AppTokenService.add(appToken).setCompletion { }
        apiManager.execute(requestBuilder)
    }
}