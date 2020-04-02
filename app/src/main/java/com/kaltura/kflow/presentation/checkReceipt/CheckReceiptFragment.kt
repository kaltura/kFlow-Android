package com.kaltura.kflow.presentation.checkReceipt

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_check_receipt.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class CheckReceiptFragment : SharedTransitionFragment(R.layout.fragment_check_receipt) {

    private val viewModel: CheckReceiptViewModel by viewModel()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.CHECK_RECEIPT

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        validate.setOnClickListener {
            hideKeyboard()
            checkReceiptRequest(receiptId.string, productType.string, productId.string, contentId.string)
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.transactionRequest,
                error = { validate.error(lifecycleScope) },
                success = { validate.success(lifecycleScope) }
        )
    }

    private fun checkReceiptRequest(receiptId: String, productType: String, productId: String, contentId: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (receiptId.isEmpty()) {
                receiptIdInputLayout.showError("Empty receipt ID")
                return@withInternetConnection
            }
            if (productType.isEmpty()) {
                productTypeInputLayout.showError("Empty product type")
                return@withInternetConnection
            }
            if (productId.isEmpty()) {
                productIdInputLayout.showError("Empty product ID")
                return@withInternetConnection
            }
            if (contentId.isEmpty()) {
                contentIdInputLayout.showError("Empty content ID")
                return@withInternetConnection
            }
            validate.startAnimation {
                viewModel.checkReceipt(receiptId, productType, productId, contentId)
            }
        }
    }

    private fun clearInputLayouts() {
        receiptIdInputLayout.hideError()
        productTypeInputLayout.hideError()
        productIdInputLayout.hideError()
        contentIdInputLayout.hideError()
    }
}