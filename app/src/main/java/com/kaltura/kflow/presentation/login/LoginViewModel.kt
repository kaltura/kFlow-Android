package com.kaltura.kflow.presentation.login

import com.kaltura.client.services.OttUserService
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel

/**
 * Created by alex_lytvynenko on 2020-01-09.
 */
class LoginViewModel(private val apiManager: PhoenixApiManager,
                     private val preferenceManager: PreferenceManager) : BaseViewModel(apiManager) {

    fun makeLoginRequest(email: String, password: String, udid: String) {
        apiManager.execute(OttUserService.login(preferenceManager.partnerId, email, password, null, udid)
                .setCompletion {
                    if (it.isSuccess) {
                        preferenceManager.ks = it.results.loginSession.ks
                        apiManager.ks = it.results.loginSession.ks
                        preferenceManager.authUser = email
                        preferenceManager.authPassword = password
                    }
                })
    }

    fun getSavedUsername() = preferenceManager.authUser

    fun getSavedPassword() = preferenceManager.authPassword
}