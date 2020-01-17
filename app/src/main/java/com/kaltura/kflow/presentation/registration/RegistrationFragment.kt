package com.kaltura.kflow.presentation.registration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.hideKeyboard
import com.kaltura.kflow.presentation.extension.string
import com.kaltura.kflow.presentation.extension.withInternetConnection
import kotlinx.android.synthetic.main.fragment_registration.*

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class RegistrationFragment : DebugFragment(R.layout.fragment_registration) {

    private val viewModel: RegistrationViewModel by viewModels()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register.setOnClickListener {
            hideKeyboard()
            makeRegistrationRequest(firstName.string, lastName.string, username.string, email.string, password.string)
        }
    }

    override fun subscribeUI() {}

    private fun makeRegistrationRequest(firstName: String, lastName: String, userName: String, email: String, password: String) {
        withInternetConnection {
            clearDebugView()
            viewModel.register(firstName, lastName, userName, email, password, PreferenceManager.with(requireContext()).partnerId)
        }
    }
}