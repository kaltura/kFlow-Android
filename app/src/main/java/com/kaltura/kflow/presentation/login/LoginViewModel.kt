package com.kaltura.kflow.presentation.login

import com.kaltura.client.services.OttUserService
import com.kaltura.client.types.LoginResponse
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.SingleLiveEvent

/**
 * Created by alex_lytvynenko on 2020-01-09.
 */
class LoginViewModel : BaseViewModel() {

    val loginResponse = SingleLiveEvent<LoginResponse>()

    fun makeLoginRequest(partnerId: Int, email: String, password: String, udid: String) {
        PhoenixApiManager.execute(OttUserService.login(partnerId, email, password, null, udid)
                .setCompletion {
                    if (it.isSuccess) loginResponse.value = it.results
                })
    }
}