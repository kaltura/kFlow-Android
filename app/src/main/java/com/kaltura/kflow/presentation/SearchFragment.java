package com.kaltura.kflow.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kaltura.client.enums.SearchHistoryOrderBy;
import com.kaltura.client.services.AssetService;
import com.kaltura.client.services.SearchHistoryService;
import com.kaltura.client.types.Asset;
import com.kaltura.client.types.FilterPager;
import com.kaltura.client.types.SearchAssetFilter;
import com.kaltura.client.types.SearchHistoryFilter;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.presentation.debug.DebugFragment;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.presentation.assetList.AssetListFragment;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.utils.Utils;

import java.text.NumberFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by alex_lytvynenko on 11/30/18.
 */
public class SearchFragment extends DebugFragment implements View.OnClickListener {

    private AppCompatTextView mHistoryCount;
    private TextInputEditText mAssetType;
    private TextInputEditText mSearchText;
    private AppCompatButton mShowAssetsButton;
    private ArrayList<Asset> mAssets = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Search");

        mHistoryCount = getView().findViewById(R.id.history_count);
        mAssetType = getView().findViewById(R.id.asset_type);
        mSearchText = getView().findViewById(R.id.search_text);
        mShowAssetsButton = getView().findViewById(R.id.show_assets);

        mShowAssetsButton.setVisibility(mAssets.isEmpty() ? View.GONE : View.VISIBLE);
        mShowAssetsButton.setText(getResources().getQuantityString(R.plurals.show_assets,
                mAssets.size(), NumberFormat.getInstance().format(mAssets.size())));

        getView().findViewById(R.id.get_search_history).setOnClickListener(this);
        getView().findViewById(R.id.search).setOnClickListener(this);
        mShowAssetsButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.get_search_history: {
                getSearchHistoryRequest();
                break;
            }
            case R.id.search: {
                searchRequest(mAssetType.getText().toString(), mSearchText.getText().toString());
                break;
            }
            case R.id.show_assets: {
                showAssets();
                break;
            }
        }
        Utils.hideKeyboard(getView());
    }

    private void searchRequest(String assetType, String kSqlSearch) {
        if (Utils.hasInternetConnection(requireContext())) {

            mAssets.clear();
            mHistoryCount.setVisibility(View.GONE);
            mShowAssetsButton.setVisibility(View.GONE);

            SearchAssetFilter filter = new SearchAssetFilter();
            ArrayList<String> assetTypes = new ArrayList<>();
            for (String s : assetType.split(",")) {
                if (!s.trim().isEmpty())
                    assetTypes.add(s.trim());
            }
            StringBuilder kSql = new StringBuilder("(or description ~ \'" + kSqlSearch + "\' name ~ \'" + kSqlSearch + "\'");
            for (String s : assetTypes) {
                kSql.append(" asset_type=\'").append(s).append("\'");
            }
            kSql.append(")");

            filter.setKSql(kSql.toString());

            FilterPager filterPager = new FilterPager();
            filterPager.setPageIndex(1);
            filterPager.setPageSize(50);

            RequestBuilder requestBuilder = AssetService.list(filter, filterPager)
                    .setCompletion(result -> {
                        if (result.isSuccess()) {
                            if (result.results.getObjects() != null)
                                mAssets.addAll(result.results.getObjects());

                            mShowAssetsButton.setText(getResources().getQuantityString(R.plurals.show_assets,
                                    mAssets.size(), NumberFormat.getInstance().format(mAssets.size())));
                            mShowAssetsButton.setVisibility(View.VISIBLE);
                        }
                    });
            PhoenixApiManager.execute(requestBuilder);
            clearDebugView();
        } else {
            Snackbar.make(getView(), "No Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss", view -> {
                    })
                    .show();
        }
    }

    private void getSearchHistoryRequest() {
        if (Utils.hasInternetConnection(requireContext())) {
            SearchHistoryFilter filter = new SearchHistoryFilter();
            filter.setOrderBy(SearchHistoryOrderBy.NONE.getValue());

            FilterPager filterPager = new FilterPager();
            filterPager.setPageIndex(1);
            filterPager.setPageSize(50);

            RequestBuilder requestBuilder = SearchHistoryService.list(filter, filterPager)
                    .setCompletion(result -> {
                        if (result.isSuccess()) {
                            mHistoryCount.setText(getResources().getQuantityString(R.plurals.history_count,
                                    result.results.getTotalCount(), NumberFormat.getInstance().format(result.results.getTotalCount())));
                            mHistoryCount.setVisibility(View.VISIBLE);
                        }
                    });
            mHistoryCount.setVisibility(View.GONE);
            clearDebugView();
            PhoenixApiManager.execute(requestBuilder);
        } else {
            Snackbar.make(getView(), "No Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss", view -> {
                    })
                    .show();
        }
    }

    private void showAssets() {
        AssetListFragment assetListFragment = AssetListFragment.newInstance(mAssets);
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
    protected int getDebugViewId() {
        return R.id.debug_view;
    }
}
