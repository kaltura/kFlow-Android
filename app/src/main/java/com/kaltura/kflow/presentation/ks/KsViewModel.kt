package com.kaltura.kflow.presentation.ks

import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.base.BaseViewModel

/**
 * Created by alex_lytvynenko on 2020-01-09.
 */
class KsViewModel(private val apiManager: PhoenixApiManager,
                  private val preferenceManager: PreferenceManager) : BaseViewModel(apiManager) {

    fun saveKs(ks: String) {
        preferenceManager.ks = ks
        apiManager.ks = ks
    }
}