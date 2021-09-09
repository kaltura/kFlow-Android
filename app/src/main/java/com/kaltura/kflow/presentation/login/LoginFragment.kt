package com.kaltura.kflow.presentation.login

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.client.types.StringValue
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.kflow.utils.getUUID
import com.kaltura.kflow.utils.getUUID2
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
            makeLoginRequest(username.string, password.string, extraParamsKey.string,
                    extraParamsDescription.string, extraParamsValue.string)
        }
        addExtraParams.setOnClickListener { showExtraParams(true) }
        removeExtraParams.setOnClickListener { showExtraParams(false) }

        username.string = viewModel.getSavedUsername()
        password.string = viewModel.getSavedPassword()
    }

    override fun subscribeUI() {
        observeResource(viewModel.loginRequest,
                error = { login.error(lifecycleScope) },
                success = { login.success(lifecycleScope) }
        )
    }

    private fun makeLoginRequest(email: String, password: String, extraParamsKey: String,
                                 extraParamsDescription: String, extraParamsValue: String) {
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

            var extraParams: HashMap<String, StringValue>? = null
            if (extraParamsKey.isNotEmpty() && extraParamsDescription.isNotEmpty() && extraParamsValue.isNotEmpty())
                extraParams = hashMapOf(extraParamsKey to StringValue().apply {
                    description = extraParamsDescription
                    value = extraParamsValue
                })

            login.startAnimation {
                viewModel.makeLoginRequest(email, password, getUUID(), extraParams)
            }
        }
    }

    private fun showExtraParams(isShow: Boolean) {
        addExtraParams.visibleOrGone(isShow.not())
        removeExtraParams.visibleOrGone(isShow)
        extraParamsKeyInputLayout.visibleOrGone(isShow)
        extraParamsDescriptionInputLayout.visibleOrGone(isShow)
        extraParamsValueInputLayout.visibleOrGone(isShow)

        if (isShow.not()) {
            extraParamsKey.text?.clear()
            extraParamsDescription.text?.clear()
            extraParamsValue.text?.clear()
        }
    }

    private fun clearInputLayouts() {
        usernameInputLayout.hideError()
        passwordInputLayout.hideError()
        extraParamsKeyInputLayout.hideError()
        extraParamsDescriptionInputLayout.hideError()
        extraParamsValueInputLayout.hideError()
    }
}