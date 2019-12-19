package com.kaltura.kflow.presentation

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.fragment.app.Fragment
import com.kaltura.client.Configuration
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.extension.string
import com.kaltura.kflow.presentation.main.MainActivity
import kotlinx.android.synthetic.main.fragment_settings.*
import org.jetbrains.anko.support.v4.toast

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Settings"

        save.setOnClickListener {
            save(url.string, partnerId.string, mediaFileFormat.string)
        }
        initUI()
    }

    private fun initUI() {
        url.string = PreferenceManager.getInstance(requireContext()).baseUrl
        partnerId.string = PreferenceManager.getInstance(requireContext()).partnerId.toString()
        mediaFileFormat.string = PreferenceManager.getInstance(requireContext()).mediaFileFormat
    }

    private fun save(baseUrl: String, partnerId: String, mediaFileFormat: String) {
        if (baseUrl.isNotEmpty()) {
            PreferenceManager.getInstance(requireContext()).clearKs()
            PreferenceManager.getInstance(requireContext()).saveBaseUrl(baseUrl)

            val config = Configuration().apply { endpoint = PreferenceManager.getInstance(requireContext()).baseUrl }
            PhoenixApiManager.getClient().connectionConfiguration = config
            PhoenixApiManager.getClient().ks = null
        } else {
            toast("END Point URL is empty")
        }
        if (partnerId.isNotEmpty() && TextUtils.isDigitsOnly(partnerId)) {
            PreferenceManager.getInstance(requireContext()).clearKs()
            PreferenceManager.getInstance(requireContext()).savePartnerId(partnerId.toInt())
            PhoenixApiManager.getClient().ks = null
        } else {
            toast("Parthner ID is missing or invalid")
        }
        if (mediaFileFormat.isNotEmpty()) PreferenceManager.getInstance(requireContext()).saveMediaFileFormat(mediaFileFormat)
        else toast("Media File Format is missing")

        toast("Saved")
    }
}