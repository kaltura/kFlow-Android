package com.kaltura.kflow.presentation.base

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.kaltura.kflow.presentation.extension.hideKeyboard

/**
 * Created by alex_lytvynenko on 11.01.2020.
 */
abstract class BaseFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeUI()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
    }

    abstract fun subscribeUI()
}