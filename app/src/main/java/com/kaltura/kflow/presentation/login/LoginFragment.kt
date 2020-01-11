package com.kaltura.kflow.presentation.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.utils.getUUID
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class LoginFragment : DebugFragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        login.setOnClickListener {
            hideKeyboard()
            makeLoginRequest(username.string, password.string)
        }
        username.string = PreferenceManager.with(requireContext()).authUser
        password.string = PreferenceManager.with(requireContext()).authPassword
    }

    override fun subscribeUI() {
        observeLiveData(viewModel.loginResponse) {
            PreferenceManager.with(requireContext()).ks = it.loginSession.ks
            PhoenixApiManager.client.ks = it.loginSession.ks
            PreferenceManager.with(requireContext()).authUser = username.string
            PreferenceManager.with(requireContext()).authPassword = password.string
        }
    }

    private fun makeLoginRequest(email: String, password: String) {
        withInternetConnection {
            clearDebugView()
            viewModel.makeLoginRequest(PreferenceManager.with(requireContext()).partnerId, email, password, getUUID(requireContext()))
        }
    }
}