package com.kaltura.kflow.presentation.subscription

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.enums.AssetOrderBy
import com.kaltura.client.enums.EntityReferenceBy
import com.kaltura.client.enums.TransactionType
import com.kaltura.client.services.AssetService
import com.kaltura.client.services.EntitlementService
import com.kaltura.client.services.SubscriptionService
import com.kaltura.client.types.*
import com.kaltura.client.utils.request.MultiRequestBuilder
import com.kaltura.kflow.R
import com.kaltura.kflow.entity.ParentRecyclerViewItem
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.presentation.assetList.AssetListFragment
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.ui.ProgressDialog
import kotlinx.android.synthetic.main.fragment_subscription.*
import org.jetbrains.anko.support.v4.toast
import kotlin.collections.ArrayList

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class SubscriptionFragment : DebugFragment(R.layout.fragment_subscription) {

    private val assets = arrayListOf<Asset>()
    private var subscriptionListAdapter = SubscriptionListAdapter(arrayListOf()).apply {
        packageGetSubscriptionListener = ::onPackageGetSubscriptionClicked
        subscriptionListener = ::onSubscriptionClicked
    }
    private lateinit var progressDialog: ProgressDialog

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initList()
        showAssets.setOnClickListener {
            hideKeyboard()
            showPackages()
        }
        getPackages.setOnClickListener {
            hideKeyboard()
            makeGetPackageListRequest(packageAssetType.string)
        }
        getEntitlements.setOnClickListener {
            hideKeyboard()
            makeGetEntitlementListRequest()
        }
        showAssets.visibleOrGone(assets.isNotEmpty())
        showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
        progressDialog = ProgressDialog(requireContext())
    }

    private fun initList() {
        packageList.isNestedScrollingEnabled = false
        packageList.layoutManager = LinearLayoutManager(requireContext())
        packageList.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
        packageList.adapter = subscriptionListAdapter
    }

    private fun makeGetPackageListRequest(packageType: String) {
        withInternetConnection {
            assets.clear()
            showAssets.gone()
            packageList.gone()
            val filter = SearchAssetFilter().apply {
                orderBy = AssetOrderBy.START_DATE_DESC.value
                kSql = "Base ID > \'0\'"
                typeIn = packageType
            }

            val filterPager = FilterPager().apply {
                pageIndex = 1
                pageSize = 40
            }

            clearDebugView()
            PhoenixApiManager.execute(AssetService.list(filter, filterPager).setCompletion {
                if (it.isSuccess) {
                    if (it.results.objects != null) assets.addAll(it.results.objects)
                    showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                    showAssets.visible()
                }
            })
        }
    }

    private fun makeGetEntitlementListRequest() {
        withInternetConnection {
            val filter = EntitlementFilter().apply {
                entityReferenceEqual = EntityReferenceBy.HOUSEHOLD
                productTypeEqual = TransactionType.SUBSCRIPTION
                isExpiredEqual = true
            }

            val filterPager = FilterPager().apply {
                pageIndex = 1
                pageSize = 40
            }

            clearDebugView()
            PhoenixApiManager.execute(EntitlementService.list(filter, filterPager))
        }
    }

    private fun getSubscriptionRequest(subscriptionBaseId: String) {
        withInternetConnection {
            val subscriptionFilter = SubscriptionFilter().apply { subscriptionIdIn = subscriptionBaseId }
            val filterPager = FilterPager().apply {
                pageIndex = 1
                pageSize = 40
            }

            clearDebugView()
            PhoenixApiManager.execute(SubscriptionService.list(subscriptionFilter, filterPager).setCompletion {
                if (it.isSuccess) {
                    subscriptionListAdapter.addSubscriptionToPackage(subscriptionBaseId.toDouble(), it.results.objects as ArrayList<Subscription>)
                }
            })
        }
    }

    private fun getAssetsInSubscription(subscriptionChannelsId: ArrayList<Long>) {
        withInternetConnection {
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
                    hideLoadingDialog()
                    if (assets.isEmpty()) toast("No assets in this subscription")
                    else navigate(SubscriptionFragmentDirections.navigateToAssetList(), AssetListFragment.ARG_ASSETS to assets)
                }
            }
            clearDebugView()
            PhoenixApiManager.execute(multiRequestBuilder)
        }
    }

    private fun showPackages() {
        packageList.visible()
        showAssets.gone()
        val packages = ArrayList<ParentRecyclerViewItem<Asset, Subscription>>()
        assets.forEach { packages.add(ParentRecyclerViewItem(it, arrayListOf())) }
        subscriptionListAdapter = SubscriptionListAdapter(packages).apply {
            packageGetSubscriptionListener = ::onPackageGetSubscriptionClicked
            subscriptionListener = ::onSubscriptionClicked
        }
        packageList.adapter = subscriptionListAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hideKeyboard()
        PhoenixApiManager.cancelAll()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        subscriptionListAdapter.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        subscriptionListAdapter.onRestoreInstanceState(savedInstanceState)
    }

    private fun onPackageGetSubscriptionClicked(packageBaseId: Double) {
        getSubscriptionRequest(packageBaseId.toInt().toString())
    }

    private fun onSubscriptionClicked(subscriptionChannelsId: ArrayList<Long>) {
        showLoadingDialog()
        getAssetsInSubscription(subscriptionChannelsId)
    }

    private fun showLoadingDialog() {
        if (!progressDialog.isShowing) progressDialog.show()
    }

    private fun hideLoadingDialog() {
        if (progressDialog.isShowing) progressDialog.dismiss()
    }
}