package com.kaltura.kflow.presentation.pushNotifications

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_push_notifications.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class PushNotificationsFragment : SharedTransitionFragment(R.layout.fragment_push_notifications) {

    private val viewModel: PushNotificationsViewModel by viewModel()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.PUSH_NOTIFICATIONS

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        register.setOnClickListener {
            makeRegisterRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.registrationEvent,
                error = { register.error(lifecycleScope) },
                success = {
                    if (it) register.success(lifecycleScope)
                    else register.error(lifecycleScope)
                })
    }

    private fun makeRegisterRequest() {
        withInternetConnection {
            clearDebugView()

            register.startAnimation {
                viewModel.register()
            }
        }
    }
}