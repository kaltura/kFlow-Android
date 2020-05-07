package com.kaltura.kflow.presentation.subscription

import androidx.lifecycle.MutableLiveData
import com.kaltura.client.enums.AssetOrderBy
import com.kaltura.client.enums.EntityReferenceBy
import com.kaltura.client.enums.TransactionType
import com.kaltura.client.services.AssetService
import com.kaltura.client.services.EntitlementService
import com.kaltura.client.services.SubscriptionService
import com.kaltura.client.types.*
import com.kaltura.client.utils.request.MultiRequestBuilder
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.base.BaseViewModel
import com.kaltura.kflow.utils.Resource

/**
 * Created by alex_lytvynenko on 2020-01-14.
 */
class SubscriptionViewModel(private val apiManager: PhoenixApiManager) : BaseViewModel(apiManager) {

    val assetList = MutableLiveData<Resource<ArrayList<Asset>>>()
    val assetsInSubscription = MutableLiveData<Resource<List<Asset>>>()
    val subscriptionList = MutableLiveData<Resource<ArrayList<Subscription>>>()
    val entitlementList = MutableLiveData<Resource<ArrayList<Entitlement>>>()

    fun getPackageList(packageType: String) {
        val filter = SearchAssetFilter().apply {
            orderBy = AssetOrderBy.START_DATE_DESC.value
            kSql = "Base ID > \'0\'"
            typeIn = packageType
        }

        val filterPager = FilterPager().apply {
            pageIndex = 1
            pageSize = 40
        }

        apiManager.execute(AssetService.list(filter, filterPager).setCompletion {
            if (it.isSuccess) {
                if (it.results.objects != null) assetList.value = Resource.Success(it.results.objects as ArrayList<Asset>)
            } else assetList.value = Resource.Error(it.error)
        })
    }

    fun getEntitlementList() {
        val filter = EntitlementFilter().apply {
            entityReferenceEqual = EntityReferenceBy.HOUSEHOLD
            productTypeEqual = TransactionType.SUBSCRIPTION
            isExpiredEqual = true
        }

        val filterPager = FilterPager().apply {
            pageIndex = 1
            pageSize = 40
        }

        apiManager.execute(EntitlementService.list(filter, filterPager)
                .setCompletion {
                    if (it.isSuccess) entitlementList.value = Resource.Success(it.results.objects as ArrayList<Entitlement>)
                    else entitlementList.value = Resource.Error(it.error)
                })
    }

    fun getSubscription(subscriptionBaseId: String) {
        val subscriptionFilter = SubscriptionFilter().apply { subscriptionIdIn = subscriptionBaseId }
        val filterPager = FilterPager().apply {
            pageIndex = 1
            pageSize = 40
        }

        apiManager.execute(SubscriptionService.list(subscriptionFilter, filterPager).setCompletion {
            if (it.isSuccess) subscriptionList.value = Resource.Success(it.results.objects as ArrayList<Subscription>)
        })
    }

    fun getAssetsInSubscription(subscriptionChannelsId: ArrayList<Long>) {
        val multiRequestBuilder = MultiRequestBuilder()
        val channelFilter = ChannelFilter()
        val filterPager = FilterPager().apply {
            pageIndex = 1
            pageSize = 40
        }

        subscriptionChannelsId.forEach {
            channelFilter.idEqual = it.toInt()
            multiRequestBuilder.add(AssetService.list(channelFilter, filterPager))
        }
        multiRequestBuilder.setCompletion {
            if (it.isSuccess && it.results != null) {
                val assets = arrayListOf<Asset>()
                it.results.forEach { assets.addAll((it as ListResponse<Asset>).objects) }
                assetsInSubscription.value = Resource.Success(assets)
            }
        }
        apiManager.execute(multiRequestBuilder)
    }
}