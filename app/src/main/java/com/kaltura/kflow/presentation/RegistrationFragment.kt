package com.kaltura.kflow.presentation

import android.os.Bundle
import android.view.View
import com.kaltura.client.services.OttUserService
import com.kaltura.client.types.OTTUser
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.string
import com.kaltura.kflow.presentation.extension.withInternetConnection
import com.kaltura.kflow.presentation.main.MainActivity
import com.kaltura.kflow.utils.Utils
import kotlinx.android.synthetic.main.fragment_registration.*

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class RegistrationFragment : DebugFragment(R.layout.fragment_registration) {

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Registration"
        register.setOnClickListener {
            Utils.hideKeyboard(getView())
            makeRegistrationRequest(firstName.string, lastName.string, username.string, email.string, password.string)
        }
    }

    private fun makeRegistrationRequest(firstName: String, lastName: String, userName: String, email: String, password: String) {
        withInternetConnection {
            val ottUser = OTTUser().apply {
                firstName(firstName)
                lastName(lastName)
                username(userName)
                email(email)
            }

            clearDebugView()
            PhoenixApiManager.execute(OttUserService.register(PreferenceManager.with(requireContext()).partnerId, ottUser, password))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Utils.hideKeyboard(view)
        PhoenixApiManager.cancelAll()
    }
}