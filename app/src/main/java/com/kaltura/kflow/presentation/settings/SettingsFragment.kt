package com.kaltura.kflow.presentation.settings

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.kaltura.client.Configuration
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentReminderListBinding
import com.kaltura.kflow.databinding.FragmentSearchBinding
import com.kaltura.kflow.databinding.FragmentSettingsBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
class SettingsFragment : SharedTransitionFragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    override val feature = Feature.SETTINGS
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSettingsBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.save.setOnClickListener {
            save(binding.url.string,binding.cloudfronturl.string, binding.partnerId.string, binding.mediaFileFormat.string)
        }
        initUI()
    }

    override fun subscribeUI() {}

    private fun initUI() {
        binding.url.string = viewModel.baseUrl
        binding.cloudfronturl.string = viewModel.cloudFrontUrl
        binding.partnerId.string = viewModel.partnerId.toString()
        binding.mediaFileFormat.string = viewModel.mediaFileFormat

        when (viewModel.urlType) {
            APIDefines.KalturaUrlType.Direct.value -> binding.urlTypeRedirect.isChecked = true
            APIDefines.KalturaUrlType.PlayManifest.value -> binding.urlTypeManifest.isChecked = true
            else -> binding.urlTypeNone.isChecked = true
        }
        when (viewModel.streamerType) {
            APIDefines.KalturaStreamerType.Mpegdash.value -> binding.streamerTypeMpegDash.isChecked = true
            APIDefines.KalturaStreamerType.Multicast.value -> binding.streamerTypeMulticast.isChecked = true
            else -> binding.streamerTypeNone.isChecked = true
        }
        when (viewModel.mediaProtocol) {
            PhoenixMediaProvider.HttpProtocol.Http -> binding.mediaProtocolHttp.isChecked = true
            PhoenixMediaProvider.HttpProtocol.Https -> binding.mediaProtocolHttps.isChecked = true
            else -> binding.mediaProtocolAll.isChecked = true
        }
        when (viewModel.codec) {
            binding.codecHevc.text -> binding.codecHevc.isChecked = true
            binding.codecAvc.text -> binding.codecAvc.isChecked = true
        }
        when (viewModel.quality) {
            binding.qualityUhd.text -> binding.qualityUhd.isChecked = true
            binding.qualityHd.text -> binding.qualityHd.isChecked = true
            binding.qualitySd.text -> binding.qualitySd.isChecked = true
        }

        binding.drm.isChecked = viewModel.drm

        binding.urlTypeTitle.setOnClickListener { longToast("Determine if the source url require redirection or not.") }
        binding.streamerTypeTitle.setOnClickListener { longToast("Require specified stream type") }
        binding.mediaProtocolTitle.setOnClickListener { longToast("Which protocol scheme is being used for accessing sources") }
        binding.codecTitle.setOnClickListener { longToast("Which codec is being used for playback") }
        binding.qualityTitle.setOnClickListener { longToast("Which quality is being used for playback") }
    }

    private fun save(baseUrl: String,cloudfrontUrl: String, partnerId: String, mediaFileFormat: String) {
        clearInputLayouts()

        if (baseUrl.isEmpty()) {
            binding.baseUrlInputLayout.showError("END Point URL is empty")
            return
        }
        if (partnerId.isEmpty()) {
            binding.partnerIdInputLayout.showError("Partner ID is missing")
            return
        }
        if (TextUtils.isDigitsOnly(partnerId).not()) {
            binding.partnerIdInputLayout.showError("Partner ID is invalid")
            return
        }
        if (mediaFileFormat.isEmpty()) {
            binding.mediaFileFormatInputLayout.showError("Media File Format is missing")
            return
        }

        viewModel.clearKs()
        viewModel.baseUrl = baseUrl
        if (cloudfrontUrl.isNotEmpty())
            viewModel.cloudFrontUrl = cloudfrontUrl
        viewModel.partnerId = partnerId.toInt()
        viewModel.mediaFileFormat = mediaFileFormat

        viewModel.urlType = when (binding.urlTypeLayout.checkedRadioButtonId) {
            binding.urlTypeRedirect.id -> APIDefines.KalturaUrlType.Direct.value
            binding.urlTypeManifest.id -> APIDefines.KalturaUrlType.PlayManifest.value
            else -> ""
        }

        viewModel.streamerType = when (binding.streamerType.checkedRadioButtonId) {
            binding.streamerTypeMpegDash.id -> APIDefines.KalturaStreamerType.Mpegdash.value
            binding.streamerTypeMulticast.id -> APIDefines.KalturaStreamerType.Multicast.value
            else -> ""
        }

        viewModel.mediaProtocol = when (binding.mediaProtocol.checkedRadioButtonId) {
            binding.mediaProtocolAll.id -> PhoenixMediaProvider.HttpProtocol.All
            binding.mediaProtocolHttp.id -> PhoenixMediaProvider.HttpProtocol.Http
            binding.mediaProtocolHttps.id -> PhoenixMediaProvider.HttpProtocol.Https
            else -> ""
        }

        viewModel.codec = when (binding.codec.checkedRadioButtonId) {
            binding.codecHevc.id -> binding.codecHevc.text.toString()
            binding.codecAvc.id -> binding.codecAvc.text.toString()
            else -> ""
        }

        viewModel.quality = when (binding.quality.checkedRadioButtonId) {
            binding.qualityUhd.id -> binding.qualityUhd.text.toString()
            binding.qualityHd.id -> binding.qualityHd.text.toString()
            binding.qualitySd.id -> binding.qualitySd.text.toString()
            else -> ""
        }

        viewModel.drm = binding.drm.isChecked

        val config = Configuration().apply { endpoint = viewModel.baseUrl }
        viewModel.setConfiguration(config)

        toast("Saved")
    }

    private fun clearInputLayouts() {
        binding.baseUrlInputLayout.hideError()
        binding.cloudfrontUrlInputLayout.hideError()
        binding.partnerIdInputLayout.hideError()
        binding.mediaFileFormatInputLayout.hideError()
    }
}