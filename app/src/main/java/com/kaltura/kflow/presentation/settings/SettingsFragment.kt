package com.kaltura.kflow.presentation.settings

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.kaltura.client.Configuration
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.BaseFragment
import com.kaltura.kflow.presentation.extension.string
import kotlinx.android.synthetic.main.fragment_settings.*
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
class SettingsFragment : BaseFragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        save.setOnClickListener {
            save(url.string, partnerId.string, mediaFileFormat.string, deviceProfile.string)
        }
        initUI()
    }

    override fun subscribeUI() {}

    private fun initUI() {
        url.string = viewModel.baseUrl
        partnerId.string = viewModel.partnerId.toString()
        mediaFileFormat.string = viewModel.mediaFileFormat
        deviceProfile.string = viewModel.deviceProfile
    }

    private fun save(baseUrl: String, partnerId: String, mediaFileFormat: String, deviceProfile: String) {
        if (baseUrl.isNotEmpty()) {
            viewModel.clearKs()
            viewModel.baseUrl = baseUrl

            val config = Configuration().apply { endpoint = viewModel.baseUrl }
            viewModel.setConfiguration(config)
        } else {
            toast("END Point URL is empty")
        }
        if (partnerId.isNotEmpty() && TextUtils.isDigitsOnly(partnerId)) {
            viewModel.clearKs()
            viewModel.partnerId = partnerId.toInt()
        } else {
            toast("Parthner ID is missing or invalid")
        }
        if (mediaFileFormat.isNotEmpty()) viewModel.mediaFileFormat = mediaFileFormat
        else toast("Media File Format is missing")

        if (deviceProfile.isNotEmpty()) viewModel.deviceProfile = deviceProfile
        else toast("Device profile is missing")

        toast("Saved")
    }
}