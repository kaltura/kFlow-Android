package com.kaltura.kflow.presentation.extension

import com.google.android.material.textfield.TextInputLayout

/**
 * Created by alex_lytvynenko on 05.03.2020.
 */

fun TextInputLayout.showError(errorMessage: String) {
    isErrorEnabled = true
    error = errorMessage
}

fun TextInputLayout.hideError() {
    isErrorEnabled = false
}