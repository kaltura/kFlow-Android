package com.kaltura.kflow.presentation.extension

import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText

/**
 * Created by alex_lytvynenko
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            afterTextChanged.invoke(p0.toString())
        }

        override fun afterTextChanged(editable: Editable?) {
        }
    })
}

fun EditText.onActionDone(action: () -> Unit) {
    this.setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
            EditorInfo.IME_ACTION_DONE -> {
                action()
                true
            }
            else -> false
        }
    }
}

fun EditText.onActionSearch(action: () -> Unit) {
    this.setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
            EditorInfo.IME_ACTION_SEARCH -> {
                action()
                true
            }
            else -> false
        }
    }
}

var EditText.string: String
    get() = this.text.toString()
    set(value) = this.setText(value)

fun EditText.trimmedString() = string.trim()