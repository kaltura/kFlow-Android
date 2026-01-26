package com.kaltura.kflow.presentation.checkReceipt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentCheckReceiptBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class CheckReceiptFragment : SharedTransitionFragment(R.layout.fragment_check_receipt) {

    private val viewModel: CheckReceiptViewModel by viewModel()

    override val feature = Feature.CHECK_RECEIPT
    private var _binding: FragmentCheckReceiptBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentCheckReceiptBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.validate.setOnClickListener {
            hideKeyboard()
            checkReceiptRequest(binding.receiptId.string, binding.productType.string, binding.productId.string, binding.contentId.string)
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.transactionRequest,
                error = { binding.validate.error(lifecycleScope) },
                success = { binding.validate.success(lifecycleScope) }
        )
    }

    private fun checkReceiptRequest(receiptId: String, productType: String, productId: String, contentId: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (receiptId.isEmpty()) {
                binding.receiptIdInputLayout.showError("Empty receipt ID")
                return@withInternetConnection
            }
            if (productType.isEmpty()) {
                binding.productTypeInputLayout.showError("Empty product type")
                return@withInternetConnection
            }
            if (productId.isEmpty()) {
                binding.productIdInputLayout.showError("Empty product ID")
                return@withInternetConnection
            }
            if (contentId.isEmpty()) {
                binding.contentIdInputLayout.showError("Empty content ID")
                return@withInternetConnection
            }
            binding.validate.startAnimation {
                viewModel.checkReceipt(receiptId, productType, productId, contentId)
            }
        }
    }

    private fun clearInputLayouts() {
        binding.receiptIdInputLayout.hideError()
        binding.productTypeInputLayout.hideError()
        binding.productIdInputLayout.hideError()
        binding.contentIdInputLayout.hideError()
    }
}