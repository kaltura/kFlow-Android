package com.kaltura.kflow.presentation.login

import android.os.Bundle
import android.view.View
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.utils.getUUID
import kotlinx.android.synthetic.main.fragment_login.*
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

    override fun subscribeUI() {}

    private fun makeLoginRequest(email: String, password: String) {
        withInternetConnection {
            clearDebugView()
            viewModel.makeLoginRequest(email, password, getUUID(requireContext()))
        }
    }
}