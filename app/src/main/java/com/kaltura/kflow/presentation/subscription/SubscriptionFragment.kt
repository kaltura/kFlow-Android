package com.kaltura.kflow.presentation.subscription

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.databinding.FragmentMediaPageBinding
import com.kaltura.kflow.databinding.FragmentSubscriptionBinding
import com.kaltura.kflow.entity.ParentRecyclerViewItem
import com.kaltura.kflow.presentation.base.SharedTransitionFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.kflow.presentation.ui.ProgressDialog
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.ArrayList

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
class SubscriptionFragment : SharedTransitionFragment(R.layout.fragment_subscription) {

    private val viewModel: SubscriptionViewModel by viewModel()
    private var assets = arrayListOf<Asset>()
    private val entitlementAdapter = EntitlementListAdapter()
    private var subscriptionListAdapter = SubscriptionListAdapter(arrayListOf()).apply {
        packageGetSubscriptionListener = ::onPackageGetSubscriptionClicked
        subscriptionListener = ::onSubscriptionClicked
    }
    private val progressDialog by lazy { ProgressDialog(activity) }
    private var selectedPackageBaseId: Double = 0.0

    override val feature = Feature.SUBSCRIPTION
    private var _binding: FragmentSubscriptionBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSubscriptionBinding.inflate(inflater,container,false)
        val view = binding.root
        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initPackageList()
        initEntitlementList()
        binding.showAssets.setOnClickListener {
            hideKeyboard()
            showPackages()
        }
        binding.getPackages.setOnClickListener {
            hideKeyboard()
            makeGetPackageListRequest(binding.packageAssetType.string)
        }
        binding.getEntitlements.setOnClickListener {
            hideKeyboard()
            makeGetEntitlementListRequest()
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.assetList,
            error = { binding.getPackages.error(lifecycleScope) },
            success = {
                assets = it
                binding.showAssets.text = getQuantityString(R.plurals.show_assets, assets.size)
                binding.showAssets.visible()
                binding.getPackages.success(lifecycleScope)
            })
        observeResource(viewModel.entitlementList,
            error = { binding.getEntitlements.error(lifecycleScope) },
            success = {
                binding.getEntitlements.success(lifecycleScope)
                showEntitlements(it)
            })
        observeResource(viewModel.subscriptionList) {
            subscriptionListAdapter.addSubscriptionToPackage(selectedPackageBaseId, it)
            binding.packageList.adapter = subscriptionListAdapter
        }
        observeResource(viewModel.assetsInSubscription) {
            hideLoadingDialog()
            if (it.isEmpty()) toast("No assets in this subscription")
            else navigate(SubscriptionFragmentDirections.navigateToAssetList(assets = it.toTypedArray()))
        }
    }

    private fun initPackageList() {
        binding.packageList.isNestedScrollingEnabled = false
        binding.packageList.layoutManager = LinearLayoutManager(requireContext())
        binding.packageList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
        binding.packageList.adapter = subscriptionListAdapter
    }

    private fun initEntitlementList() {
        binding.entitlementList.isNestedScrollingEnabled = false
        binding.entitlementList.layoutManager = LinearLayoutManager(requireContext())
        binding.entitlementList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                LinearLayoutManager.VERTICAL
            )
        )
        binding.entitlementList.adapter = entitlementAdapter
    }

    private fun makeGetPackageListRequest(packageType: String) {
        withInternetConnection {
            clearDebugView()
            clearInputLayouts()

            if (packageType.isEmpty()) {
                binding.packageAssetTypeInputLayout.showError("Empty package type")
                return@withInternetConnection
            }

            binding.showAssets.gone()
            binding.packageList.gone()
            binding.entitlementList.gone()

            binding.getPackages.startAnimation {
                viewModel.getPackageList(packageType)
            }
        }
    }

    private fun clearInputLayouts() {
        binding.packageAssetTypeInputLayout.hideError()
    }

    private fun makeGetEntitlementListRequest() {
        withInternetConnection {
            clearDebugView()

            binding.showAssets.gone()
            binding.packageList.gone()
            binding.entitlementList.gone()

            binding.getEntitlements.startAnimation {
                viewModel.getEntitlementList()
            }
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
        binding.packageList.visible()
        binding.entitlementList.gone()
        binding.showAssets.gone()
        val packages = ArrayList<ParentRecyclerViewItem<Asset, Subscription>>()
        assets.forEach { packages.add(ParentRecyclerViewItem(it, arrayListOf())) }
        subscriptionListAdapter = SubscriptionListAdapter(packages).apply {
            packageGetSubscriptionListener = ::onPackageGetSubscriptionClicked
            subscriptionListener = ::onSubscriptionClicked
        }
        binding.packageList.adapter = subscriptionListAdapter
    }

    private fun showEntitlements(entitlements: ArrayList<Entitlement>) {
        binding.entitlementList.visible()
        entitlementAdapter.entitlements = entitlements
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
        selectedPackageBaseId = packageBaseId
        getSubscriptionRequest(packageBaseId.toInt().toString())
    }

    private fun onSubscriptionClicked(subscriptionChannelsId: ArrayList<Long>) {
        showLoadingDialog()
        getAssetsInSubscription(subscriptionChannelsId)
    }

    private fun showLoadingDialog() {
        if (progressDialog.isShowing.not()) progressDialog.show()
    }

    private fun hideLoadingDialog() {
        if (progressDialog.isShowing) progressDialog.dismiss()
    }
}