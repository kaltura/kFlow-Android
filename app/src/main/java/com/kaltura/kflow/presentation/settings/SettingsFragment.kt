package com.kaltura.kflow.presentation.settings

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.kaltura.client.Configuration
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.hideError
import com.kaltura.kflow.presentation.extension.showError
import com.kaltura.kflow.presentation.extension.string
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.tvplayer.KalturaOttPlayer
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
class SettingsFragment : SharedTransitionFragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.SETTINGS

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        save.setOnClickListener {
            save(url.string, partnerId.string, mediaFileFormat.string)
        }
        initUI()
    }

    override fun subscribeUI() {}

    private fun initUI() {
        url.string = viewModel.baseUrl
        partnerId.string = viewModel.partnerId.toString()
        mediaFileFormat.string = viewModel.mediaFileFormat
    }

    private fun save(baseUrl: String, partnerId: String, mediaFileFormat: String) {
        clearInputLayouts()

        if (baseUrl.isEmpty()) {
            baseUrlInputLayout.showError("END Point URL is empty")
            return
        }
        if (partnerId.isEmpty()) {
            partnerIdInputLayout.showError("Partner ID is missing")
            return
        }
        if (TextUtils.isDigitsOnly(partnerId).not()) {
            partnerIdInputLayout.showError("Partner ID is invalid")
            return
        }
        if (mediaFileFormat.isEmpty()) {
            mediaFileFormatInputLayout.showError("Media File Format is missing")
            return
        }

        viewModel.clearKs()
        viewModel.baseUrl = baseUrl
        viewModel.partnerId = partnerId.toInt()
        viewModel.mediaFileFormat = mediaFileFormat

        KalturaOttPlayer.initialize(activity, viewModel.partnerId, viewModel.baseUrl)

        val config = Configuration().apply { endpoint = viewModel.baseUrl }
        viewModel.setConfiguration(config)

        toast("Saved")
    }

    private fun clearInputLayouts() {
        baseUrlInputLayout.hideError()
        partnerIdInputLayout.hideError()
        mediaFileFormatInputLayout.hideError()
    }
}