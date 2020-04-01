package com.kaltura.kflow.presentation.login

import com.kaltura.client.services.UserService
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel

/**
 * Created by alex_lytvynenko on 2020-01-09.
 */
class LoginViewModel(private val apiManager: PhoenixApiManager,
                     private val preferenceManager: PreferenceManager) : BaseViewModel(apiManager) {

    fun makeLoginRequest(email: String, password: String, udid: String) {
        preferenceManager.clearKs()
        apiManager.ks = null
        apiManager.execute(UserService.login(preferenceManager.partnerId, email, password)
                .setCompletion {
                    if (it.isSuccess) {
                        preferenceManager.ks = it.results
                        apiManager.ks = it.results
                        preferenceManager.authUser = email
                        preferenceManager.authPassword = password
                    }
                })
    }

    fun getSavedUsername() = preferenceManager.authUser

    fun getSavedPassword() = preferenceManager.authPassword
}