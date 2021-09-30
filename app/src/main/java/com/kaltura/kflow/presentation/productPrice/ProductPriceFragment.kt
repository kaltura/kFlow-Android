package com.kaltura.kflow.presentation.productPrice

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import kotlinx.android.synthetic.main.fragment_product_price.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class ProductPriceFragment : SharedTransitionFragment(R.layout.fragment_product_price) {

    private val viewModel: ProductPriceViewModel by viewModel()
    private var productPrices = arrayListOf<ProductPrice>()

    override fun debugView(): DebugView = debugView
    override val feature = Feature.PRODUCT_PRICE

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initList()
        showProductPrices.setOnClickListener {
            hideKeyboard()
            showProductPrices()
        }
        get.setOnClickListener {
            hideKeyboard()
            makeGetAssetRequest(assetId.string, coupon.string)
        }
    }

    private fun initList() {
        productPriceList.isNestedScrollingEnabled = false
        productPriceList.layoutManager = LinearLayoutManager(requireContext())
        productPriceList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
        productPriceList.adapter = ProductPriceListAdapter(arrayListOf())
    }

    override fun subscribeUI() {
        observeResource(viewModel.productPriceList,
            error = { get.error(lifecycleScope) },
            success = {
                get.success(lifecycleScope)
                productPrices = it
                showProductPrices.text =
                    getQuantityString(R.plurals.show_product_prices, productPrices.size)
                showProductPrices.visible()
            })
    }

    private fun makeGetAssetRequest(assetId: String, couponCode: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (assetId.isEmpty()) {
                assetIdInputLayout.showError("Empty asset ID")
                return@withInternetConnection
            }

            showProductPrices.gone()
            productPriceList.gone()

            get.startAnimation {
                viewModel.getProductPrices(assetId, couponCode)
            }
        }
    }

    private fun clearInputLayouts() {
        assetIdInputLayout.hideError()
    }

    private fun showProductPrices() {
        productPriceList.visible()
        showProductPrices.gone()
        productPriceList.adapter = ProductPriceListAdapter(productPrices).apply {
            onSubscriptionPriceClickListener = {

            }
        }
    }

    private fun showAssets(assetList: ArrayList<Asset>) {
        navigate(ProductPriceFragmentDirections.navigateToAssetList(assets = assetList.toTypedArray()))
    }
}