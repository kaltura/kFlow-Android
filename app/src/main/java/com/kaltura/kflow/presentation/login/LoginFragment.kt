package com.kaltura.kflow.presentation.login

import android.os.Bundle
import android.view.View
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.utils.getUUID
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class LoginFragment : DebugFragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModel()

    override fun debugView(): DebugView = debugView

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
        observeResource(viewModel.loginRequest, error = {
            login.doneLoadingAnimation(getColor(R.color.red), getDrawable(R.drawable.ic_error_outline_white_24dp)!!.toBitmap())
            lifecycleScope.launch {
                delay(2000)
                login.revertAnimation()
            }
        }, success = {
            login.doneLoadingAnimation(getColor(R.color.green), getDrawable(R.drawable.ic_done_white_24dp)!!.toBitmap())
            lifecycleScope.launch {
                delay(2000)
                login.revertAnimation()
            }
        })
    }

    private fun makeLoginRequest(email: String, password: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()
            if (email.isEmpty()) {
                usernameInputLayout.isErrorEnabled = true
                usernameInputLayout.error = "Empty username"
                return@withInternetConnection
            }
            if (password.isEmpty()) {
                passwordInputLayout.isErrorEnabled = true
                passwordInputLayout.error = "Empty password"
                return@withInternetConnection
            }
            login.startAnimation {
                viewModel.makeLoginRequest(email, password, getUUID(requireContext()))
            }
        }
    }

    private fun clearInputLayouts() {
        usernameInputLayout.isErrorEnabled = false
        passwordInputLayout.isErrorEnabled = false
    }
}