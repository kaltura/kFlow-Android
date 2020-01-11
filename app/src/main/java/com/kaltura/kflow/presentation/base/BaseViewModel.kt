package com.kaltura.kflow.presentation.base

import androidx.lifecycle.ViewModel
import com.kaltura.kflow.manager.PhoenixApiManager

/**
 * Created by alex_lytvynenko on 11.01.2020.
 */
open class BaseViewModel: ViewModel() {

    override fun onCleared() {
        super.onCleared()
        PhoenixApiManager.cancelAll()
    }
}