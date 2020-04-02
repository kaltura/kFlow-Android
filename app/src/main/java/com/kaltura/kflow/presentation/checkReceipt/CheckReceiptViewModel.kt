package com.kaltura.kflow.presentation.checkReceipt

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.TransactionType
import com.kaltura.client.services.TransactionService
import com.kaltura.client.types.ExternalReceipt
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 2020-01-16.
 */
class CheckReceiptViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val transactionRequest = MutableLiveData<Resource<Unit>>()

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
        apiManager.execute(TransactionService.validateReceipt(externalReceipt)
                .setCompletion {
                    if (it.isSuccess) transactionRequest.value = Resource.Success(Unit)
                    else transactionRequest.value = Resource.Error(it.error)
                })
    }
}