package com.kaltura.kflow.presentation.deviceManagement

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
import kotlinx.android.synthetic.main.fragment_device_management.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class DeviceManagementFragment : SharedTransitionFragment(R.layout.fragment_device_management) {

    private val viewModel: DeviceManagementViewModel by viewModel()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.DEVICE_MANAGEMENT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        removeDeviceRequest.setOnClickListener {
            hideKeyboard()
            removeDeviceFromHousehold()
        }
        addDeviceRequest.setOnClickListener {
            hideKeyboard()
            addDeviceToHousehold()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.removeDevice,
                error = { removeDeviceRequest.error(lifecycleScope) },
                success = {
                    removeDeviceRequest.success(lifecycleScope)
                    if (it) Snackbar.make(requireView(), "Device was removed!", Snackbar.LENGTH_SHORT).show()
                })
        observeResource(viewModel.addDevice,
                error = { addDeviceRequest.error(lifecycleScope) },
                success = {
                    addDeviceRequest.success(lifecycleScope)
                    if (it) Snackbar.make(requireView(), "Device was added!", Snackbar.LENGTH_SHORT).show()
                })
    }

    private fun removeDeviceFromHousehold() {
        withInternetConnection {
            clearDebugView()

            removeDeviceRequest.startAnimation {
                viewModel.removeDeviceFromHousehold(getUUID(requireContext()))
            }
        }
    }

    private fun addDeviceToHousehold() {
        withInternetConnection {
            clearDebugView()

            addDeviceRequest.startAnimation {
                viewModel.addDeviceToHousehold("${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}", getUUID(requireContext()))
            }
        }
    }
}