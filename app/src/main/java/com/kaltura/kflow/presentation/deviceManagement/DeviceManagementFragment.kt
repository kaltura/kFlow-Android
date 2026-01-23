package com.kaltura.kflow.presentation.deviceManagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentDeviceManagementBinding
import com.kaltura.kflow.databinding.FragmentIotBinding
import com.kaltura.kflow.databinding.ViewDebugBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.kflow.utils.getUUID
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class DeviceManagementFragment : SharedTransitionFragment(R.layout.fragment_device_management) {

    private val viewModel: DeviceManagementViewModel by viewModel()

    override val feature = Feature.DEVICE_MANAGEMENT
    private var _binding: FragmentDeviceManagementBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentDeviceManagementBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.removeDeviceRequest.setOnClickListener {
            hideKeyboard()
            removeDeviceFromHousehold()
        }
        binding.addDeviceRequest.setOnClickListener {
            hideKeyboard()
            addDeviceToHousehold()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.removeDevice,
                error = { binding.removeDeviceRequest.error(lifecycleScope) },
                success = {
                    binding.removeDeviceRequest.success(lifecycleScope)
                    if (it) Snackbar.make(requireView(), "Device was removed!", Snackbar.LENGTH_SHORT).show()
                })
        observeResource(viewModel.addDevice,
                error = { binding.addDeviceRequest.error(lifecycleScope) },
                success = {
                    binding.addDeviceRequest.success(lifecycleScope)
                    if (it) Snackbar.make(requireView(), "Device was added!", Snackbar.LENGTH_SHORT).show()
                })
    }

    private fun removeDeviceFromHousehold() {
        withInternetConnection {
            clearDebugView()

            binding.removeDeviceRequest.startAnimation {
                viewModel.removeDeviceFromHousehold(getUUID())
            }
        }
    }

    private fun addDeviceToHousehold() {
        withInternetConnection {
            clearDebugView()

            binding.addDeviceRequest.startAnimation {
                viewModel.addDeviceToHousehold("${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}", getUUID())
            }
        }
    }
}