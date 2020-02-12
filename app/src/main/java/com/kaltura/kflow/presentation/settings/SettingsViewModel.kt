package com.kaltura.kflow.presentation.settings

import com.kaltura.client.Configuration
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel

/**
 * Created by alex_lytvynenko on 2020-01-09.
 */
class SettingsViewModel(private val apiManager: PhoenixApiManager,
                        private val preferenceManager: PreferenceManager) : BaseViewModel(apiManager) {

    var partnerId: Int
        get() = preferenceManager.partnerId
        set(value) {
            preferenceManager.partnerId = value
        }

    var baseUrl: String
        get() = preferenceManager.baseUrl
        set(value) {
            preferenceManager.baseUrl = value
        }

    var mediaFileFormat: String
        get() = preferenceManager.mediaFileFormat
        set(value) {
            preferenceManager.mediaFileFormat = value
        }

    fun clearKs() {
        preferenceManager.clearKs()
        apiManager.ks = null
    }

    fun setConfiguration(config: Configuration) {
        apiManager.setConnectionConfiguration(config)
    }
}