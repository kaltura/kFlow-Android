package com.kaltura.kflow.presentation.anonymousLogin

import com.kaltura.client.enums.AppTokenHashType
import com.kaltura.client.services.AppTokenService
import com.kaltura.client.services.OttUserService
import com.kaltura.client.types.AppToken
import com.kaltura.client.types.LoginSession
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.SingleLiveEvent

/**
 * Created by alex_lytvynenko on 2020-01-15.
 */
class AnonymousLoginViewModel : BaseViewModel() {

    val loginSession = SingleLiveEvent<LoginSession>()

    fun anonymousLogin(partnerId: Int, udid: String) {
        PhoenixApiManager.execute(OttUserService.anonymousLogin(partnerId, udid)
                .setCompletion {
                    if (it.isSuccess) loginSession.value = it.results
                })
    }

    fun generateAppToken() {
        val appToken = AppToken()
        appToken.hashType = AppTokenHashType.SHA256
        appToken.sessionDuration = 604800 // 604800 seconds = 7 days
        appToken.expiry = 1832668157
        val requestBuilder = AppTokenService.add(appToken).setCompletion { }
        PhoenixApiManager.execute(requestBuilder)
    }
}