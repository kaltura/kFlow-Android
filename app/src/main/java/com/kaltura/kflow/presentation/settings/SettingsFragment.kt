package com.kaltura.kflow.presentation.settings

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.kaltura.client.Configuration
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
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

        when (viewModel.urlType) {
            APIDefines.KalturaUrlType.Direct.value -> urlTypeRedirect.isChecked = true
            APIDefines.KalturaUrlType.PlayManifest.value -> urlTypeManifest.isChecked = true
            else -> urlTypeNone.isChecked = true
        }
        when (viewModel.streamerType) {
            APIDefines.KalturaStreamerType.Mpegdash.value -> streamerTypeMpegDash.isChecked = true
            else -> streamerTypeNone.isChecked = true
        }
        when (viewModel.mediaProtocol) {
            PhoenixMediaProvider.HttpProtocol.Http -> mediaProtocolHttp.isChecked = true
            PhoenixMediaProvider.HttpProtocol.Https -> mediaProtocolHttps.isChecked = true
            else -> mediaProtocolAll.isChecked = true
        }

        urlTypeTitle.setOnClickListener { longToast("Determine if the source url require redirection or not.") }
        streamerTypeTitle.setOnClickListener { longToast("Require specified stream type") }
        mediaProtocolTitle.setOnClickListener { longToast("Which protocol scheme is being used for accessing sources") }
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

        viewModel.urlType = when (urlTypeLayout.checkedRadioButtonId) {
            urlTypeRedirect.id -> APIDefines.KalturaUrlType.Direct.value
            urlTypeManifest.id -> APIDefines.KalturaUrlType.PlayManifest.value
            else -> ""
        }

        viewModel.streamerType = when (streamerType.checkedRadioButtonId) {
            streamerTypeMpegDash.id -> APIDefines.KalturaStreamerType.Mpegdash.value
            else -> ""
        }

        viewModel.mediaProtocol = when (mediaProtocol.checkedRadioButtonId) {
            mediaProtocolAll.id -> PhoenixMediaProvider.HttpProtocol.All
            mediaProtocolHttp.id -> PhoenixMediaProvider.HttpProtocol.Http
            mediaProtocolHttps.id -> PhoenixMediaProvider.HttpProtocol.Https
            else -> ""
        }

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