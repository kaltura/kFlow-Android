package com.kaltura.kflow.presentation.transactionHistory

import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.enums.EntityReferenceBy
import com.kaltura.client.services.TransactionHistoryService
import com.kaltura.client.types.BillingTransaction
import com.kaltura.client.types.TransactionHistoryFilter
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.synthetic.main.fragment_transaction_history.*

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class TransactionHistoryFragment : DebugFragment(R.layout.fragment_transaction_history) {
    private val transactions = arrayListOf<BillingTransaction>()
    private val transactionHistoryListAdapter = TransactionHistoryListAdapter()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initList()
        showTransactions.setOnClickListener {
            hideKeyboard()
            showTransactions()
        }
        get.setOnClickListener {
            hideKeyboard()
            makeGetTransactionHistoryRequest()
        }
        showTransactions.visibleOrGone(transactions.isNotEmpty())
        showTransactions.text = resources.getQuantityString(R.plurals.show_transactions, transactions.size)
    }

    private fun initList() {
        transactionsList.isNestedScrollingEnabled = false
        transactionsList.layoutManager = LinearLayoutManager(requireContext())
        transactionsList.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        transactionsList.adapter = transactionHistoryListAdapter
    }

    private fun makeGetTransactionHistoryRequest() {
        withInternetConnection {
            val transactionHistoryFilter = TransactionHistoryFilter().apply {
                entityReferenceEqual = EntityReferenceBy.HOUSEHOLD
                endDateLessThanOrEqual = ((System.currentTimeMillis() + DateUtils.YEAR_IN_MILLIS) / 1000).toInt()
            }

            clearDebugView()
            PhoenixApiManager.execute(TransactionHistoryService.list(transactionHistoryFilter).setCompletion {
                if (it.isSuccess && it.results != null) {
                    if (it.results.objects != null) transactions.addAll(it.results.objects)
                    showTransactions.text = getQuantityString(R.plurals.show_transactions, transactions.size)
                    showTransactions.visible()
                }
            })
        }
    }

    private fun showTransactions() {
        transactionsList.visible()
        showTransactions.gone()
        transactionHistoryListAdapter.transactions = transactions
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        PhoenixApiManager.cancelAll()
    }
}