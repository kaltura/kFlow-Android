package com.kaltura.kflow.presentation.anonymousLogin

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.kflow.utils.getUUID
import kotlinx.android.synthetic.main.fragment_anonymous_login.login
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class AnonymousLoginFragment : SharedTransitionFragment(R.layout.fragment_anonymous_login) {

    private val viewModel: AnonymousLoginViewModel by viewModel()

    override fun debugView(): DebugView = debugView

    override val feature = Feature.ANONYMOUS_LOGIN

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        login.setOnClickListener {
            hideKeyboard()
            makeAnonymousLoginRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.loginRequest,
                error = { login.error(lifecycleScope) },
                success = { login.success(lifecycleScope) }
        )
    }

    private fun makeAnonymousLoginRequest() {
        withInternetConnection {
            clearDebugView()
            login.startAnimation {
                viewModel.anonymousLogin(getUUID(requireContext()))
            }
        }
    }
}