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
import com.kaltura.kflow.presentation.extension.withInternetConnection
import com.kaltura.kflow.presentation.main.MainActivity
import com.kaltura.kflow.utils.Utils
import kotlinx.android.synthetic.main.fragment_anonymous_login.*

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class AnonymousLoginFragment : DebugFragment(R.layout.fragment_anonymous_login) {

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Anonymous login"
        login.setOnClickListener {
            Utils.hideKeyboard(getView())
            makeAnonymousLoginRequest()
        }
    }

    private fun makeAnonymousLoginRequest() {
        withInternetConnection {
            clearDebugView()
            PhoenixApiManager.execute(
                    OttUserService.anonymousLogin(PreferenceManager.with(requireContext()).partnerId, Utils.getUUID(requireContext()))
                            .setCompletion {
                                if (it.isSuccess) {
                                    PreferenceManager.with(requireContext()).ks = it.results.ks
                                    PhoenixApiManager.getClient().ks = it.results.ks
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

    override fun onDestroyView() {
        super.onDestroyView()
        Utils.hideKeyboard(view)
        PhoenixApiManager.cancelAll()
    }
}