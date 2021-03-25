package com.kaltura.kflow.presentation.ks

import android.os.Bundle
import android.view.View
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_ks.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
class KsFragment : SharedTransitionFragment(R.layout.fragment_ks) {

    private val viewModel: KsViewModel by viewModel()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.WORK_WITH_KS

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        save.setOnClickListener {
            hideKeyboard()
            saveKs(ks.string)
        }
    }

    override fun subscribeUI() {}

    private fun saveKs(ksValue: String) {
        if (ksValue.isEmpty()) {
            ksInputLayout.showError("Empty KS")
            return
        }

        viewModel.saveKs(ksValue)

        toast("Saved")
    }
}