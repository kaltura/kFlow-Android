package com.kaltura.kflow.presentation.transactionHistory

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.types.BillingTransaction
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_transaction_history.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class TransactionHistoryFragment : SharedTransitionFragment(R.layout.fragment_transaction_history) {

    private val viewModel: TransactionHistoryViewModel by viewModel()
    private var transactions = arrayListOf<BillingTransaction>()
    private val transactionHistoryListAdapter = TransactionHistoryListAdapter()

    override fun debugView(): DebugView = debugView

    override val feature = Feature.TRANSACTION_HISTORY

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
    }

    override fun subscribeUI() {
        observeResource(viewModel.billingTransactions,
                error = { get.error(lifecycleScope) },
                success = {
                    get.success(lifecycleScope)

                    transactions = it
                    showTransactions.text = getQuantityString(R.plurals.show_transactions, transactions.size)
                    showTransactions.visible()
                }
        )
    }

    private fun initList() {
        transactionsList.isNestedScrollingEnabled = false
        transactionsList.layoutManager = LinearLayoutManager(requireContext())
        transactionsList.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        transactionsList.adapter = transactionHistoryListAdapter
    }

    private fun makeGetTransactionHistoryRequest() {
        withInternetConnection {
            clearDebugView()
            showTransactions.gone()
            transactionsList.gone()
            get.startAnimation {
                viewModel.getTransactionsHistory()
            }
        }
    }

    private fun showTransactions() {
        transactionsList.visible()
        showTransactions.gone()
        transactionHistoryListAdapter.transactions = transactions
    }
}