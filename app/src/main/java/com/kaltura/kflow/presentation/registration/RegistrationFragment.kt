package com.kaltura.kflow.presentation.registration

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_registration.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class RegistrationFragment : SharedTransitionFragment(R.layout.fragment_registration) {

    private val viewModel: RegistrationViewModel by viewModel()
    override val feature = Feature.REGISTRATION

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register.setOnClickListener {
            hideKeyboard()
            makeRegistrationRequest(firstName.string, lastName.string, username.string, email.string, password.string)
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.registerRequest,
                error = { register.error(lifecycleScope) },
                success = { register.success(lifecycleScope) }
        )
    }

    private fun makeRegistrationRequest(firstName: String, lastName: String, userName: String, email: String, password: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()
            if (firstName.isEmpty()) {
                firstNameInputLayout.showError("Empty first name")
                return@withInternetConnection
            }
            if (lastName.isEmpty()) {
                lastNameInputLayout.showError("Empty last name")
                return@withInternetConnection
            }
            if (userName.isEmpty()) {
                userNameInputLayout.showError("Empty username")
                return@withInternetConnection
            }
            if (email.isEmpty()) {
                emailInputLayout.showError("Empty email")
                return@withInternetConnection
            }
            if (password.isEmpty()) {
                passwordInputLayout.showError("Empty password")
                return@withInternetConnection
            }
            register.startAnimation {
                viewModel.register(firstName, lastName, userName, email, password)
            }
        }
    }

    private fun clearInputLayouts() {
        firstNameInputLayout.hideError()
        lastNameInputLayout.hideError()
        userNameInputLayout.hideError()
        emailInputLayout.hideError()
        passwordInputLayout.hideError()
    }
}