package com.kaltura.kflow.presentation.subscription;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kaltura.client.enums.AssetOrderBy;
import com.kaltura.client.enums.EntityReferenceBy;
import com.kaltura.client.enums.TransactionType;
import com.kaltura.client.services.AssetService;
import com.kaltura.client.services.EntitlementService;
import com.kaltura.client.services.SubscriptionService;
import com.kaltura.client.types.Asset;
import com.kaltura.client.types.ChannelFilter;
import com.kaltura.client.types.EntitlementFilter;
import com.kaltura.client.types.FilterPager;
import com.kaltura.client.types.ListResponse;
import com.kaltura.client.types.SearchAssetFilter;
import com.kaltura.client.types.SubscriptionFilter;
import com.kaltura.client.utils.request.MultiRequestBuilder;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.client.utils.response.base.ApiCompletion;
import com.kaltura.kflow.R;
import com.kaltura.kflow.entity.ParentRecyclerViewItem;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.presentation.assetList.AssetListFragment;
import com.kaltura.kflow.presentation.debug.DebugFragment;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.presentation.ui.ProgressDialog;
import com.kaltura.kflow.utils.Utils;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
public class SubscriptionFragment extends DebugFragment implements View.OnClickListener, SubscriptionListAdapter.OnPackageClickListener {

    private TextInputEditText mPackageAssetType;
    private AppCompatButton mShowAssetsButton;
    private RecyclerView mPackageList;
    private ArrayList<Asset> mAssets = new ArrayList<>();
    private SubscriptionListAdapter mSubscriptionListAdapter;
    private ProgressDialog mProgressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subscription, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Subscription");

        mPackageAssetType = getView().findViewById(R.id.package_asset_type);
        mShowAssetsButton = getView().findViewById(R.id.show_assets);
        initList();

        mShowAssetsButton.setOnClickListener(this);
        getView().findViewById(R.id.get_packages).setOnClickListener(this);
        getView().findViewById(R.id.get_entitlements).setOnClickListener(this);

        mShowAssetsButton.setVisibility(mAssets.isEmpty() ? View.GONE : View.VISIBLE);
        mShowAssetsButton.setText(getResources().getQuantityString(R.plurals.show_assets,
                mAssets.size(), NumberFormat.getInstance().format(mAssets.size())));

        mProgressDialog = new ProgressDialog(getContext());
    }

    private void initList() {
        mPackageList = getView().findViewById(R.id.package_list);
        mPackageList.setNestedScrollingEnabled(false);
        mPackageList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mPackageList.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));
        mSubscriptionListAdapter = new SubscriptionListAdapter(new ArrayList<>(), this);
        mPackageList.setAdapter(mSubscriptionListAdapter);
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());

        switch (view.getId()) {
            case R.id.show_assets: {
                showPackages();
                break;
            }
            case R.id.get_packages: {
                makeGetPackageListRequest(mPackageAssetType.getText().toString());
                break;
            }
            case R.id.get_entitlements: {
                makeGetEntitlementListRequest();
                break;
            }
        }
    }

    private void makeGetPackageListRequest(String packageType) {
        if (Utils.hasInternetConnection(requireContext())) {

            mAssets.clear();
            mShowAssetsButton.setVisibility(View.GONE);
            mPackageList.setVisibility(View.GONE);

            SearchAssetFilter filter = new SearchAssetFilter();
            filter.setOrderBy(AssetOrderBy.START_DATE_DESC.getValue());
            filter.setKSql("Base ID > \'0\'");
            filter.setTypeIn(packageType);

            FilterPager filterPager = new FilterPager();
            filterPager.setPageIndex(1);
            filterPager.setPageSize(40);

            RequestBuilder requestBuilder = AssetService.list(filter, filterPager)
                    .setCompletion((ApiCompletion<ListResponse<Asset>>) result -> {
                        if (result.isSuccess()) {
                            if (result.results.getObjects() != null)
                                mAssets.addAll(result.results.getObjects());

                            mShowAssetsButton.setText(getResources().getQuantityString(R.plurals.show_assets,
                                    mAssets.size(), NumberFormat.getInstance().format(mAssets.size())));
                            mShowAssetsButton.setVisibility(View.VISIBLE);
                        }
                    });
            clearDebugView();
            PhoenixApiManager.execute(requestBuilder);
        } else {
            Snackbar.make(getView(), "No Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss",view -> {})
                    .show();
        }
    }

    private void makeGetEntitlementListRequest() {
        if (Utils.hasInternetConnection(requireContext())) {

            EntitlementFilter filter = new EntitlementFilter();
            filter.setEntityReferenceEqual(EntityReferenceBy.HOUSEHOLD);
            filter.setProductTypeEqual(TransactionType.SUBSCRIPTION);
            filter.setIsExpiredEqual(true);

            FilterPager filterPager = new FilterPager();
            filterPager.setPageIndex(1);
            filterPager.setPageSize(40);

            RequestBuilder requestBuilder = EntitlementService.list(filter, filterPager);
            clearDebugView();
            PhoenixApiManager.execute(requestBuilder);
        } else {
            Snackbar.make(getView(), "No Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss",view -> {})
                    .show();
        }
    }

    private void getSubscriptionRequest(String subscriptionBaseId) {
        if (Utils.hasInternetConnection(requireContext())) {

            SubscriptionFilter subscriptionFilter = new SubscriptionFilter();
            subscriptionFilter.setSubscriptionIdIn(subscriptionBaseId);

            FilterPager filterPager = new FilterPager();
            filterPager.setPageIndex(1);
            filterPager.setPageSize(40);

            RequestBuilder requestBuilder = SubscriptionService.list(subscriptionFilter, filterPager)
                    .setCompletion(result -> {
                        if (result.isSuccess()) {
                            mSubscriptionListAdapter.addSubscriptionToPackage(Double.parseDouble(subscriptionBaseId), result.results.getObjects());
                        }
                    });
            clearDebugView();
            PhoenixApiManager.execute(requestBuilder);
        } else {
            Snackbar.make(getView(), "No Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss",view -> {})
                    .show();
        }
    }

    private void getAssetsInSubscription(ArrayList<Long> subscriptionChannelsId) {
        if (Utils.hasInternetConnection(requireContext())) {

            MultiRequestBuilder multiRequestBuilder = new MultiRequestBuilder();

            ChannelFilter channelFilter = new ChannelFilter();

            FilterPager filterPager = new FilterPager();
            filterPager.setPageIndex(1);
            filterPager.setPageSize(40);

            for (Long channelId : subscriptionChannelsId) {
                channelFilter.setIdEqual(channelId.intValue());
                multiRequestBuilder.add(AssetService.list(channelFilter, filterPager));
            }

            multiRequestBuilder.setCompletion(result -> {
                if (result.isSuccess() && result.results != null) {
                    ArrayList<Asset> assets = new ArrayList<>();
                    for (Object listResponse : result.results) {
                        assets.addAll(((ListResponse) listResponse).getObjects());
                    }
                    hideLoadingDialog();

                    if (assets.isEmpty())
                        Toast.makeText(requireContext(), "No assets in this subscription", Toast.LENGTH_LONG).show();
                    else showAssets(assets);
                }
            });

            clearDebugView();
            PhoenixApiManager.execute(multiRequestBuilder);
        } else {
            Snackbar.make(getView(), "No Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss",view -> {})
                    .show();
        }
    }

    private void showPackages() {
        mPackageList.setVisibility(View.VISIBLE);
        mShowAssetsButton.setVisibility(View.GONE);
        ArrayList<ParentRecyclerViewItem> packages = new ArrayList<>();
        for (Asset asset : mAssets) {
            packages.add(new ParentRecyclerViewItem(asset, new ArrayList()));
        }
        mSubscriptionListAdapter = new SubscriptionListAdapter(packages, this);
        mPackageList.setAdapter(mSubscriptionListAdapter);
    }

    private void showAssets(ArrayList<Asset> assets) {
        AssetListFragment assetListFragment = AssetListFragment.newInstance(assets);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, assetListFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getView());
        PhoenixApiManager.cancelAll();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSubscriptionListAdapter.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mSubscriptionListAdapter.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected int getDebugViewId() {
        return R.id.debug_view;
    }

    @Override
    public void onPackageGetSubscriptionClicked(Double packageBaseId) {
        getSubscriptionRequest(String.valueOf(packageBaseId.intValue()));
    }

    @Override
    public void onSubscriptionClicked(ArrayList<Long> subscriptionChannelsId) {
        showLoadingDialog();
        getAssetsInSubscription(subscriptionChannelsId);
    }

    private void showLoadingDialog() {
        if (!mProgressDialog.isShowing()) mProgressDialog.show();
    }

    private void hideLoadingDialog() {
        if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
    }
}