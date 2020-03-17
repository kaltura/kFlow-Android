package com.kaltura.kflow.presentation.transactionHistory

import android.text.format.DateUtils
import androidx.lifecycle.MutableLiveData
//import com.kaltura.client.enums.EntityReferenceBy
//import com.kaltura.client.services.TransactionHistoryService
//import com.kaltura.client.types.BillingTransaction
//import com.kaltura.client.types.TransactionHistoryFilter
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 2020-01-15.
 */
class TransactionHistoryViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

//    val billingTransactions = MutableLiveData<Resource<ArrayList<BillingTransaction>>>()

    fun getTransactionsHistory() {
//        val transactionHistoryFilter = TransactionHistoryFilter().apply {
//            entityReferenceEqual = EntityReferenceBy.HOUSEHOLD
//            endDateLessThanOrEqual = ((System.currentTimeMillis() + DateUtils.YEAR_IN_MILLIS) / 1000).toInt()
//        }
//
//        apiManager.execute(TransactionHistoryService.list(transactionHistoryFilter).setCompletion {
//            if (it.isSuccess && it.results != null) {
//                if (it.results.objects != null) billingTransactions.value = Resource.Success(it.results.objects as ArrayList<BillingTransaction>)
//            }
//        })
    }
}