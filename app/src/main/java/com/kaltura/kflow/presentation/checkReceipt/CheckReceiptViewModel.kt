package com.kaltura.kflow.presentation.checkReceipt

import com.kaltura.client.enums.TransactionType
import com.kaltura.client.services.TransactionService
import com.kaltura.client.types.ExternalReceipt
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel

/**
 * Created by alex_lytvynenko on 2020-01-16.
 */
class CheckReceiptViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    fun checkReceipt(receiptId: String, productType: String, productId: String, contentId: String) {
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
        apiManager.execute(TransactionService.validateReceipt(externalReceipt))
    }
}