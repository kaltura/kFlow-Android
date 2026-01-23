package com.kaltura.kflow.presentation.productPrice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentPlayerBinding
import com.kaltura.kflow.databinding.FragmentProductPriceBinding
import com.kaltura.kflow.databinding.FragmentSubscriptionBinding
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class ProductPriceFragment : SharedTransitionFragment(R.layout.fragment_product_price) {

    private val viewModel: ProductPriceViewModel by viewModel()
    private var productPrices = arrayListOf<ProductPrice>()

    override val feature = Feature.PRODUCT_PRICE
    private var _binding: FragmentProductPriceBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentProductPriceBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initList()
        binding.showProductPrices.setOnClickListener {
            hideKeyboard()
            showProductPrices()
        }
        binding.get.setOnClickListener {
            hideKeyboard()
            makeGetAssetRequest(binding.assetId.string, binding.coupon.string)
        }
    }

    private fun initList() {
        binding.productPriceList.isNestedScrollingEnabled = false
        binding.productPriceList.layoutManager = LinearLayoutManager(requireContext())
        binding.productPriceList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
        binding.productPriceList.adapter = ProductPriceListAdapter(arrayListOf())
    }

    override fun subscribeUI() {
        observeResource(viewModel.productPriceList,
            error = { binding.get.error(lifecycleScope) },
            success = {
                binding.get.success(lifecycleScope)
                productPrices = it
                binding.showProductPrices.text =
                    getQuantityString(R.plurals.show_product_prices, productPrices.size)
                binding.showProductPrices.visible()
            })
    }

    private fun makeGetAssetRequest(assetId: String, couponCode: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                binding.assetIdInputLayout.showError("Empty asset ID")
                return@withInternetConnection
            }

            binding.showProductPrices.gone()
            binding.productPriceList.gone()

            binding.get.startAnimation {
                viewModel.getProductPrices(assetId, couponCode)
            }
        }
    }

    private fun clearInputLayouts() {
        binding.assetIdInputLayout.hideError()
    }

    private fun showProductPrices() {
        binding.productPriceList.visible()
        binding.showProductPrices.gone()
        binding.productPriceList.adapter = ProductPriceListAdapter(productPrices).apply {
            onSubscriptionPriceClickListener = {

            }
        }
    }

    private fun showAssets(assetList: ArrayList<Asset>) {
        navigate(ProductPriceFragmentDirections.navigateToAssetList(assets = assetList.toTypedArray()))
    }
}