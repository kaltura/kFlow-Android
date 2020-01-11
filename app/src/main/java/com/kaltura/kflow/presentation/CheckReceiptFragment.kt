package com.kaltura.kflow.presentation

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.kaltura.client.enums.TransactionType
import com.kaltura.client.services.TransactionService
import com.kaltura.client.types.ExternalReceipt
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.hideKeyboard
import com.kaltura.kflow.presentation.extension.string
import com.kaltura.kflow.presentation.extension.withInternetConnection
import kotlinx.android.synthetic.main.fragment_check_receipt.*
import org.jetbrains.anko.support.v4.toast

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class CheckReceiptFragment : DebugFragment(R.layout.fragment_check_receipt) {

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
                val externalReceipt = ExternalReceipt().apply {
                    this.receiptId = receiptId
                    if (productType.equals(TransactionType.PPV.value, ignoreCase = true) ||
                            productType.equals(TransactionType.SUBSCRIPTION.value, ignoreCase = true) ||
                            productType.equals(TransactionType.COLLECTION.value, ignoreCase = true)) {
                        this.productType = TransactionType.get(productType.toLowerCase())
                    }
                    this.productId = productId.toInt()
                    this.contentId = contentId.toInt()
                    this.paymentGatewayName = "PGAdapterGoogle"
                }
                PhoenixApiManager.execute(TransactionService.validateReceipt(externalReceipt))
            } else {
                toast("Wrong input")
            }
        }
    }
}