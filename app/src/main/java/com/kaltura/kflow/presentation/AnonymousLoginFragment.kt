package com.kaltura.kflow.presentation

import android.os.Bundle
import android.view.View
import com.kaltura.client.enums.AppTokenHashType
import com.kaltura.client.services.AppTokenService
import com.kaltura.client.services.OttUserService
import com.kaltura.client.types.AppToken
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.hideKeyboard
import com.kaltura.kflow.presentation.extension.withInternetConnection
import com.kaltura.kflow.utils.getUUID
import kotlinx.android.synthetic.main.fragment_anonymous_login.*

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class AnonymousLoginFragment : DebugFragment(R.layout.fragment_anonymous_login) {

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        login.setOnClickListener {
            hideKeyboard()
            makeAnonymousLoginRequest()
        }
    }

    override fun subscribeUI() {}

    private fun makeAnonymousLoginRequest() {
        withInternetConnection {
            clearDebugView()
            PhoenixApiManager.execute(
                    OttUserService.anonymousLogin(PreferenceManager.with(requireContext()).partnerId, getUUID(requireContext()))
                            .setCompletion {
                                if (it.isSuccess) {
                                    PreferenceManager.with(requireContext()).ks = it.results.ks
                                    PhoenixApiManager.client.ks = it.results.ks
                                    //generateAppToken();
                                }
                            })
        }
    }

    private fun generateAppToken() {
        val appToken = AppToken()
        appToken.hashType = AppTokenHashType.SHA256
        appToken.sessionDuration = 604800 // 604800 seconds = 7 days
        appToken.expiry = 1832668157
        val requestBuilder = AppTokenService.add(appToken).setCompletion { }
        PhoenixApiManager.execute(requestBuilder)
        clearDebugView()
    }
}