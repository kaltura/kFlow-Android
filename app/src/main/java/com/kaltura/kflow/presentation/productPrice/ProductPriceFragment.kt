package com.kaltura.kflow.presentation.productPrice

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.enums.AssetReferenceType
import com.kaltura.client.services.AssetService
import com.kaltura.client.services.ProductPriceService
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.assetList.AssetListFragment
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.MainActivity
import kotlinx.android.synthetic.main.fragment_product_price.*
import java.util.*

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class ProductPriceFragment : DebugFragment(R.layout.fragment_product_price) {
    private val productPrices = arrayListOf<ProductPrice>()

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Product price"

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
        showProductPrices.visibleOrGone(productPrices.isNotEmpty())
        showProductPrices.text = getQuantityString(R.plurals.show_product_prices, productPrices.size)
    }

    private fun initList() {
        productPriceList.isNestedScrollingEnabled = false
        productPriceList.layoutManager = LinearLayoutManager(requireContext())
        productPriceList.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        productPriceList.adapter = ProductPriceListAdapter(arrayListOf())
    }

    private fun makeGetAssetRequest(assetId: String) {
        withInternetConnection {
            productPrices.clear()
            showProductPrices.gone()
            productPriceList.gone()
            clearDebugView()
            PhoenixApiManager.execute(AssetService.get(assetId, AssetReferenceType.MEDIA).setCompletion {
                if (it.isSuccess) makeGetProductPricesRequest(it.results)
            })
        }
    }

    private fun makeGetProductPricesRequest(asset: Asset) {
        withInternetConnection {
            val fileIdInString = StringBuilder()
            asset.mediaFiles?.let {
                asset.mediaFiles.forEach {
                    if (asset.mediaFiles.indexOf(it) != 0)
                        fileIdInString.append(", ")

                    fileIdInString.append(it.id.toString())
                }
            }
            val productPriceFilter = ProductPriceFilter().apply { fileIdIn = fileIdInString.toString() }
            clearDebugView()
            PhoenixApiManager.execute(ProductPriceService.list(productPriceFilter).setCompletion {
                if (it.isSuccess && it.results != null) {
                    if (it.results.objects != null)
                        productPrices.addAll(it.results.objects)

                    showProductPrices.text = getQuantityString(R.plurals.show_product_prices, productPrices.size)
                    showProductPrices.visible()
                }
            })
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
        replaceFragment(instanceOf<AssetListFragment>(AssetListFragment.ARG_ASSETS to assetList), addToBackStack = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        PhoenixApiManager.cancelAll()
    }
}