package com.kaltura.kflow.presentation.login

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.kflow.utils.getUUID
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class LoginFragment : SharedTransitionFragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModel()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.LOGIN

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        login.setOnClickListener {
            hideKeyboard()
            makeLoginRequest(username.string, password.string)
        }
        username.string = viewModel.getSavedUsername()
        password.string = viewModel.getSavedPassword()
    }

    override fun subscribeUI() {
        observeResource(viewModel.loginRequest,
                error = { login.error(lifecycleScope) },
                success = { login.success(lifecycleScope) }
        )
    }

    private fun makeLoginRequest(email: String, password: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()
            if (email.isEmpty()) {
                usernameInputLayout.showError("Empty username")
                return@withInternetConnection
            }
            if (password.isEmpty()) {
                passwordInputLayout.showError("Empty password")
                return@withInternetConnection
            }
            login.startAnimation {
                viewModel.makeLoginRequest(email, password, getUUID(requireContext()))
            }
        }
    }

    private fun clearInputLayouts() {
        usernameInputLayout.hideError()
        passwordInputLayout.hideError()
    }
}