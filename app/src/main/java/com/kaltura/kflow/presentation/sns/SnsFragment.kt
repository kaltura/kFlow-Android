package com.kaltura.kflow.presentation.sns
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_sns.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class SnsFragment : SharedTransitionFragment(R.layout.fragment_sns) {

    private val viewModel: SNSViewModel by viewModel()
    override fun debugView(): DebugView = debugView
    override val feature = Feature.SNS
    var isPushEnabled = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        push_enable_status.gone()
        register_sns.setOnClickListener {
            makeSetDevicePushTokenRequest()
        }
        push_enable_status.setOnClickListener {
            makeSetPushStatusRequest(!isPushEnabled)
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.pushRegistrationEvent,
            error = {
                it.printStackTrace()
                longToast("Register SNS Failed : $it")
            },
            success = {
                longToast("Register SNS Success")
                makeGetNotificationStatus()
            })
        observeResource(viewModel.getPushStatusEvent,
            error = {
                it.printStackTrace()
                longToast("Getting Push Status Failed : $it")
                register_sns.error(lifecycleScope)
                push_enable_status.gone()
            },
            success = {
                isPushEnabled = it
                push_enable_status.visible()
                register_sns.success(lifecycleScope)
                when (it) {
                    true -> push_enable_status.text = getText(R.string.disable_push_notification)//"Disable Push Notification"
                    false -> push_enable_status.text = getText(R.string.enable_push_notification)//"Enable Push Notification"

                }
            })
        observeResource(viewModel.setPushStatusEvent,
            error = {
                it.printStackTrace()
                longToast("Setting Push Status Failed : $it")
            },
            success = {
                push_enable_status.visible()
                push_enable_status.success(lifecycleScope)
                push_enable_status.post {
                    isPushEnabled = !isPushEnabled
                    when (isPushEnabled) {
                        true -> push_enable_status.text = getText(R.string.disable_push_notification)//"Disable Push Notification"
                        false -> push_enable_status.text = getText(R.string.enable_push_notification)//"Enable  Push Notification"
                    }
                }

                toast("Push Status is set to :"+isPushEnabled)
            })
    }

    private fun makeSetDevicePushTokenRequest() {
        withInternetConnection {
            hideKeyboard()
            clearDebugView()

            register_sns.startAnimation {
                viewModel.setDevicePushTokenRequest()
            }
        }
    }

    private fun makeGetNotificationStatus() {
        withInternetConnection {
            clearDebugView()
            viewModel.getNotificationStatus()
        }
    }
    private fun makeSetPushStatusRequest(checked: Boolean) {
        withInternetConnection {
            clearDebugView()
            push_enable_status.startAnimation {
                viewModel.setNotificationStatus(checked)
            }
        }
    }
}