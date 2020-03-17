package com.kaltura.kflow.presentation.subscription

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.entity.ParentRecyclerViewItem
import com.kaltura.kflow.presentation.assetList.AssetListFragment
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.ui.ProgressDialog
import kotlinx.android.synthetic.main.fragment_subscription.*
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.ArrayList

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class SubscriptionFragment : DebugFragment(R.layout.fragment_subscription) {

    private val viewModel: SubscriptionViewModel by viewModel()
    private var assets = arrayListOf<Asset>()
//    private var subscriptionListAdapter = SubscriptionListAdapter(arrayListOf()).apply {
//        packageGetSubscriptionListener = ::onPackageGetSubscriptionClicked
//        subscriptionListener = ::onSubscriptionClicked
//    }
    private lateinit var progressDialog: ProgressDialog
    private var selectedPackageBaseId: Double = 0.0

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
        progressDialog = ProgressDialog(requireContext())
    }

    override fun subscribeUI() {
        observeResource(viewModel.assetList) {
            assets = it
            showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
            showAssets.visible()
        }
//        observeResource(viewModel.subscriptionList) {
//            subscriptionListAdapter.addSubscriptionToPackage(selectedPackageBaseId, it)
//        }
        observeResource(viewModel.assetsInSubscription) {
            hideLoadingDialog()
            if (it.isEmpty()) toast("No assets in this subscription")
            else navigate(SubscriptionFragmentDirections.navigateToAssetList(), AssetListFragment.ARG_ASSETS to it)
        }
    }

    private fun initList() {
        packageList.isNestedScrollingEnabled = false
        packageList.layoutManager = LinearLayoutManager(requireContext())
        packageList.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
//        packageList.adapter = subscriptionListAdapter
    }

    private fun makeGetPackageListRequest(packageType: String) {
        withInternetConnection {
            showAssets.gone()
            packageList.gone()
            clearDebugView()
            viewModel.getPackageList(packageType)
        }
    }

    private fun makeGetEntitlementListRequest() {
        withInternetConnection {
            clearDebugView()
            viewModel.getEntitlementList()
        }
    }

    private fun getSubscriptionRequest(subscriptionBaseId: String) {
        withInternetConnection {
            clearDebugView()
            viewModel.getSubscription(subscriptionBaseId)
        }
    }

    private fun getAssetsInSubscription(subscriptionChannelsId: ArrayList<Long>) {
        withInternetConnection {
            clearDebugView()
            viewModel.getAssetsInSubscription(subscriptionChannelsId)
        }
    }

    private fun showPackages() {
//        packageList.visible()
//        showAssets.gone()
//        val packages = ArrayList<ParentRecyclerViewItem<Asset, Subscription>>()
//        assets.forEach { packages.add(ParentRecyclerViewItem(it, arrayListOf())) }
//        subscriptionListAdapter = SubscriptionListAdapter(packages).apply {
//            packageGetSubscriptionListener = ::onPackageGetSubscriptionClicked
//            subscriptionListener = ::onSubscriptionClicked
//        }
//        packageList.adapter = subscriptionListAdapter
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        subscriptionListAdapter.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
//        subscriptionListAdapter.onRestoreInstanceState(savedInstanceState)
    }

    private fun onPackageGetSubscriptionClicked(packageBaseId: Double) {
        selectedPackageBaseId = packageBaseId
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