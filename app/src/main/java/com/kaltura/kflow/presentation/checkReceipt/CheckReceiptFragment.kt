package com.kaltura.kflow.presentation.checkReceipt

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.hideKeyboard
import com.kaltura.kflow.presentation.extension.string
import com.kaltura.kflow.presentation.extension.withInternetConnection
import kotlinx.android.synthetic.main.fragment_check_receipt.*
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class CheckReceiptFragment : DebugFragment(R.layout.fragment_check_receipt) {

    private val viewModel: CheckReceiptViewModel by viewModel()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        validate.setOnClickListener {
            hideKeyboard()
            checkReceiptRequest(receiptId.string, productType.string, productId.string, contentId.string)
        }
    }

    override fun subscribeUI() {}

    private fun checkReceiptRequest(receiptId: String, productType: String, productId: String, contentId: String) {
        if (receiptId.isEmpty() || productType.isEmpty() || productId.isEmpty() || contentId.isEmpty()) {
            toast("Wrong input, please fill in all the fields")
            return
        }
        withInternetConnection {
            if (TextUtils.isDigitsOnly(productId) && TextUtils.isDigitsOnly(contentId)) {
                clearDebugView()
                viewModel.checkReceipt(receiptId, productType, productId, contentId)
            } else {
                toast("Wrong input")
            }
        }
    }
}