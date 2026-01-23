package com.kaltura.kflow.presentation.transactionHistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.types.BillingTransaction
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentCheckReceiptBinding
import com.kaltura.kflow.databinding.FragmentSubscriptionBinding
import com.kaltura.kflow.databinding.FragmentTransactionHistoryBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class TransactionHistoryFragment : SharedTransitionFragment(R.layout.fragment_transaction_history) {

    private val viewModel: TransactionHistoryViewModel by viewModel()
    private var transactions = arrayListOf<BillingTransaction>()
    private val transactionHistoryListAdapter = TransactionHistoryListAdapter()

    override val feature = Feature.TRANSACTION_HISTORY
    private var _binding: FragmentTransactionHistoryBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentTransactionHistoryBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initList()
        binding.showTransactions.setOnClickListener {
            hideKeyboard()
            showTransactions()
        }
        binding.get.setOnClickListener {
            hideKeyboard()
            makeGetTransactionHistoryRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.billingTransactions,
                error = { binding.get.error(lifecycleScope) },
                success = {
                    binding.get.success(lifecycleScope)

                    transactions = it
                    binding.showTransactions.text = getQuantityString(R.plurals.show_transactions, transactions.size)
                    binding.showTransactions.visible()
                }
        )
    }

    private fun initList() {
        binding.transactionsList.isNestedScrollingEnabled = false
        binding.transactionsList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsList.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        binding.transactionsList.adapter = transactionHistoryListAdapter
    }

    private fun makeGetTransactionHistoryRequest() {
        withInternetConnection {
            clearDebugView()
            binding.showTransactions.gone()
            binding.transactionsList.gone()
            binding.get.startAnimation {
                viewModel.getTransactionsHistory()
            }
        }
    }

    private fun showTransactions() {
        binding.transactionsList.visible()
        binding.showTransactions.gone()
        transactionHistoryListAdapter.transactions = transactions
    }
}