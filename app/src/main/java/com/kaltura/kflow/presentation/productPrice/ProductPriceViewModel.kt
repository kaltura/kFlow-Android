package com.kaltura.kflow.presentation.productPrice

import androidx.lifecycle.MutableLiveData
//import com.kaltura.client.enums.AssetReferenceType
//import com.kaltura.client.services.AssetService
//import com.kaltura.client.services.ProductPriceService
import com.kaltura.client.types.Asset
//import com.kaltura.client.types.ProductPrice
//import com.kaltura.client.types.ProductPriceFilter
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 2020-01-14.
 */
class ProductPriceViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

//    val productPriceList = MutableLiveData<Resource<ArrayList<ProductPrice>>>()

    fun getProductPrices(assetId: String) {
        makeGetAssetRequest(assetId)
    }

    private fun makeGetAssetRequest(assetId: String) {
//        apiManager.execute(AssetService.get(assetId, AssetReferenceType.MEDIA).setCompletion {
//            if (it.isSuccess) makeGetProductPricesRequest(it.results)
//        })
    }

//    private fun makeGetProductPricesRequest(asset: Asset) {
//        val fileIdInString = StringBuilder()
//        asset.mediaFiles?.let {
//            asset.mediaFiles.forEach {
//                if (asset.mediaFiles.indexOf(it) != 0)
//                    fileIdInString.append(", ")
//
//                fileIdInString.append(it.id.toString())
//            }
//        }
//        val productPriceFilter = ProductPriceFilter().apply { fileIdIn = fileIdInString.toString() }
//        apiManager.execute(ProductPriceService.list(productPriceFilter).setCompletion {
//            if (it.isSuccess && it.results != null) {
//                if (it.results.objects != null) productPriceList.value = Resource.Success(it.results.objects as ArrayList<ProductPrice>)
//            }
//        })
//    }
}