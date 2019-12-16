package com.kaltura.kflow.presentation

import android.os.Bundle
import android.view.View
import com.kaltura.client.services.OttUserService
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.string
import com.kaltura.kflow.presentation.extension.withInternetConnection
import com.kaltura.kflow.presentation.main.MainActivity
import com.kaltura.kflow.utils.Utils
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class LoginFragment : DebugFragment(R.layout.fragment_login) {

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Login"

        login.setOnClickListener {
            Utils.hideKeyboard(view)
            makeLoginRequest(username.string, password.string)
        }
        username.string = PreferenceManager.getInstance(requireContext()).authUser
        password.string = PreferenceManager.getInstance(requireContext()).authPassword
    }

    private fun makeLoginRequest(email: String, password: String) {
        withInternetConnection {
            clearDebugView()
            PhoenixApiManager.execute(OttUserService.login(PreferenceManager.getInstance(requireContext()).partnerId, email, password,
                    null, Utils.getUUID(requireContext()))
                    .setCompletion {
                        if (it.isSuccess) {
                            PreferenceManager.getInstance(requireContext()).saveKs(it.results.loginSession.ks)
                            PhoenixApiManager.getClient().ks = it.results.loginSession.ks
                            PreferenceManager.getInstance(requireContext()).saveAuthUser(email)
                            PreferenceManager.getInstance(requireContext()).saveAuthPassword(password)
                        }
                    })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Utils.hideKeyboard(view)
        PhoenixApiManager.cancelAll()
    }
}