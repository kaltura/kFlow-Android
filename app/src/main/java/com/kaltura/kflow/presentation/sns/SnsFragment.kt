package com.kaltura.kflow.presentation.sns
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentSnsBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

class SnsFragment : SharedTransitionFragment(R.layout.fragment_sns) {

    private val viewModel: SNSViewModel by viewModel()

    override val feature = Feature.SNS
    var isPushEnabled = false
    private var _binding: FragmentSnsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSnsBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.pushEnableStatus.gone()
        binding.registerSns.setOnClickListener {
            makeSetDevicePushTokenRequest()
        }
        binding.pushEnableStatus.setOnClickListener {
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
                binding.registerSns.error(lifecycleScope)
                binding.pushEnableStatus.gone()
            },
            success = {
                isPushEnabled = it
                binding.pushEnableStatus.visible()
                binding.registerSns.success(lifecycleScope)
                when (it) {
                    true -> binding.pushEnableStatus.text = getText(R.string.disable_push_notification)//"Disable Push Notification"
                    false -> binding.pushEnableStatus.text = getText(R.string.enable_push_notification)//"Enable Push Notification"

                }
            })
        observeResource(viewModel.setPushStatusEvent,
            error = {
                it.printStackTrace()
                longToast("Setting Push Status Failed : $it")
            },
            success = {
                binding.pushEnableStatus.visible()
                binding.pushEnableStatus.success(lifecycleScope)
                binding.pushEnableStatus.post {
                    isPushEnabled = !isPushEnabled
                    when (isPushEnabled) {
                        true -> binding.pushEnableStatus.text = getText(R.string.disable_push_notification)//"Disable Push Notification"
                        false -> binding.pushEnableStatus.text = getText(R.string.enable_push_notification)//"Enable  Push Notification"
                    }
                }

                toast("Push Status is set to :"+isPushEnabled)
            })
    }

    private fun makeSetDevicePushTokenRequest() {
        withInternetConnection {
            hideKeyboard()
            clearDebugView()

            binding.registerSns.startAnimation {
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
            binding.pushEnableStatus.startAnimation {
                viewModel.setNotificationStatus(checked)
            }
        }
    }
}