package com.kaltura.kflow.presentation.appToken

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.kflow.utils.getUUID
import kotlinx.android.synthetic.main.fragment_app_token.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class AppTokenFragment : SharedTransitionFragment(R.layout.fragment_app_token) {

    private val viewModel: AppTokenViewModel by viewModel()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.LOGIN_APP_TOKEN

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        explicitLogin.setOnClickListener {
            hideKeyboard()
            makeExplicitLoginRequest(username.string, password.string)
        }
        appToken.setOnClickListener {
            hideKeyboard()
            makeAppTokenStartSessionRequest()
        }
        revokeSession.setOnClickListener {
            hideKeyboard()
            makeRevokeSessionRequest()
        }

        username.string = viewModel.getSavedUsername()
        password.string = viewModel.getSavedPassword()
    }

    override fun subscribeUI() {
        observeResource(viewModel.loginRequest,
                error = {
                    explicitLogin.error(lifecycleScope)
                    appToken.error(lifecycleScope)
                },
                success = {
                    explicitLogin.success(lifecycleScope)
                    appToken.success(lifecycleScope)
                    viewModel.saveUserCreds(username.string, password.string)
                }
        )
        observeResource(viewModel.revokeSessionRequest,
                error = { revokeSession.error(lifecycleScope) },
                success = { revokeSession.success(lifecycleScope) }
        )
        observeResource(viewModel.anonymousLoginRequest,
                error = {
                    explicitLogin.error(lifecycleScope)
                    appToken.error(lifecycleScope)
                },
                success = {
                    explicitLogin.error(lifecycleScope)
                    appToken.error(lifecycleScope)
                    Snackbar.make(requireView(), "Anonymous login!", Snackbar.LENGTH_LONG).show()
                }
        )
    }

    private fun makeExplicitLoginRequest(email: String, password: String) {
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

            explicitLogin.startAnimation {
                viewModel.makeExplicitLoginRequest(email, password, getUUID(requireContext()))
            }
        }
    }

    private fun makeAppTokenStartSessionRequest() {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            appToken.startAnimation {
                viewModel.makeAppTokenStartRequest(getUUID(requireContext()))
            }
        }
    }

    private fun makeRevokeSessionRequest() {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            revokeSession.startAnimation {
                viewModel.makeRevokeSessionRequest()
            }
        }
    }

    private fun clearInputLayouts() {
        usernameInputLayout.hideError()
        passwordInputLayout.hideError()
    }
}