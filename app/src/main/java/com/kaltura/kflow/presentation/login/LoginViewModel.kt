package com.kaltura.kflow.presentation.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kaltura.client.services.OttUserService
import com.kaltura.client.types.LoginResponse
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager

/**
 * Created by alex_lytvynenko on 2020-01-09.
 */
class LoginViewModel : ViewModel() {

    val loginResponse = MutableLiveData<LoginResponse>()

    fun makeLoginRequest(partnerId: Int, email: String, password: String, udid: String) {
//        PhoenixApiManager.execute(OttUserService.login(partnerId, email, password, null, udid)
//                .setCompletion {
//                    if (it.isSuccess) {
//                        loginResponse.value = it.results
//                        PreferenceManager.with(requireContext()).ks = it.results.loginSession.ks
//                        PhoenixApiManager.client.ks = it.results.loginSession.ks
//                        PreferenceManager.with(requireContext()).authUser = email
//                        PreferenceManager.with(requireContext()).authPassword = password
//                    }
//                })
    }
}