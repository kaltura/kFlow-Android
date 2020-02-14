package com.kaltura.kflow.presentation.productPrice

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.assetList.AssetListFragment
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import kotlinx.android.synthetic.main.fragment_product_price.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class ProductPriceFragment : DebugFragment(R.layout.fragment_product_price) {

    private val viewModel: ProductPriceViewModel by viewModel()
    private var productPrices = arrayListOf<ProductPrice>()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initList()
        showProductPrices.setOnClickListener {
            hideKeyboard()
            showProductPrices()
        }
        get.setOnClickListener {
            hideKeyboard()
            makeGetAssetRequest(assetId.string)
        }
        assetId.string = "428755"
    }

    private fun initList() {
        productPriceList.isNestedScrollingEnabled = false
        productPriceList.layoutManager = LinearLayoutManager(requireContext())
        productPriceList.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        productPriceList.adapter = ProductPriceListAdapter(arrayListOf())
    }

    override fun subscribeUI() {
        observeResource(viewModel.productPriceList) {
            productPrices = it
            showProductPrices.text = getQuantityString(R.plurals.show_product_prices, productPrices.size)
            showProductPrices.visible()
        }
    }

    private fun makeGetAssetRequest(assetId: String) {
        withInternetConnection {
            showProductPrices.gone()
            productPriceList.gone()
            clearDebugView()
            viewModel.getProductPrices(assetId)
        }
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
        navigate(ProductPriceFragmentDirections.navigateToAssetList(), AssetListFragment.ARG_ASSETS to assetList)
    }
}