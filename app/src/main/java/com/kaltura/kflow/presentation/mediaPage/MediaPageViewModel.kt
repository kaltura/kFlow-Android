package com.kaltura.kflow.presentation.mediaPage

import androidx.lifecycle.MutableLiveData
//import com.kaltura.client.enums.AssetReferenceType
//import com.kaltura.client.enums.PinType
import com.kaltura.client.services.*
import com.kaltura.client.types.*
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 20.01.2020.
 */
class MediaPageViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val mediaEntry = MutableLiveData<Resource<MediaEntry>>()
//    val userAssetRules = MutableLiveData<Resource<ArrayList<UserAssetRule>>>()

    fun getMediaEntry(entryId: String) {
        apiManager.execute(MediaService.get(entryId).setCompletion {
            if (it.isSuccess) {
                if (it.results != null) mediaEntry.value = Resource.Success(it.results)
            }
        })
    }

    fun getProductPrice(assetId: String) {
//        val productPriceFilter = ProductPriceFilter().apply { fileIdIn = assetId }
//        apiManager.execute(ProductPriceService.list(productPriceFilter))
    }

    fun getBookmark(assetId: String) {
//        val bookmarkFilter = BookmarkFilter().apply {
//            assetIdIn = assetId
//            assetTypeEqual = AssetType.MEDIA
//        }
//        apiManager.execute(BookmarkService.list(bookmarkFilter))
    }

    fun getAssetRules(assetId: String) {
//        val userAssetRuleFilter = UserAssetRuleFilter().apply {
//            assetTypeEqual = 1
//            assetIdEqual = assetId.toLong()
//        }
//        apiManager.execute(UserAssetRuleService.list(userAssetRuleFilter).setCompletion {
//            if (it.isSuccess) {
//                if (it.results.objects != null) userAssetRules.value = Resource.Success(it.results.objects as ArrayList<UserAssetRule>)
//            }
//        })
    }

    fun checkPin(pin: String, parentalRuleId: Int) {
//        apiManager.execute(PinService.validate(pin, PinType.PARENTAL, parentalRuleId))
    }

    fun checkAllTogether(assetId: String) {
//        val multiRequestBuilder = MultiRequestBuilder()
//        // product price request
//        val productPriceFilter = ProductPriceFilter().apply { fileIdIn = assetId }
//        multiRequestBuilder.add(ProductPriceService.list(productPriceFilter))
//        // bookmark request
//        val bookmarkFilter = BookmarkFilter().apply {
//            assetIdIn = assetId
//            assetTypeEqual = AssetType.MEDIA
//        }
//
//        multiRequestBuilder.add(BookmarkService.list(bookmarkFilter))
//        // asset rules request
//        val userAssetRuleFilter = UserAssetRuleFilter().apply {
//            assetTypeEqual = 1
//            assetIdEqual = assetId.toLong()
//        }
//
//        multiRequestBuilder.add(UserAssetRuleService.list(userAssetRuleFilter))
//        multiRequestBuilder.setCompletion {
//            if (it.isSuccess) {
//                if (it.results != null && it.results[2] != null) {
//                    userAssetRules.value = Resource.Success((it.results[2] as ListResponse<UserAssetRule>).objects as ArrayList<UserAssetRule>)
//                }
//            }
//        }
//        apiManager.execute(multiRequestBuilder)
    }
}