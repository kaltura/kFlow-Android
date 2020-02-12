package com.kaltura.kflow.presentation.anonymousLogin

import android.os.Bundle
import android.view.View
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.hideKeyboard
import com.kaltura.kflow.presentation.extension.withInternetConnection
import com.kaltura.kflow.utils.getUUID
import kotlinx.android.synthetic.main.fragment_anonymous_login.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class AnonymousLoginFragment : DebugFragment(R.layout.fragment_anonymous_login) {

    private val viewModel: AnonymousLoginViewModel by viewModel()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        login.setOnClickListener {
            hideKeyboard()
            makeAnonymousLoginRequest()
        }
    }

    override fun subscribeUI() {}

    private fun makeAnonymousLoginRequest() {
        withInternetConnection {
            clearDebugView()
            viewModel.anonymousLogin(getUUID(requireContext()))
        }
    }
}