package com.kaltura.kflow.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.kaltura.kflow.ui.debug.DebugFragment;
import com.kaltura.kflow.ui.main.MainActivity;
import com.kaltura.kflow.ui.assetList.AssetListFragment;
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
    private TextInputEditText mTypeIn;
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
        mTypeIn = getView().findViewById(R.id.type_in);
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
                searchRequest(mTypeIn.getText().toString(), mSearchText.getText().toString());
                break;
            }
            case R.id.show_assets: {
                showAssets();
                break;
            }
        }
        Utils.hideKeyboard(getView());
    }

    private void searchRequest(String typeIn, String kSql) {
        if (Utils.hasInternetConnection(requireContext())) {

            mAssets.clear();
            mHistoryCount.setVisibility(View.GONE);
            mShowAssetsButton.setVisibility(View.GONE);

            SearchAssetFilter filter = new SearchAssetFilter();
            filter.setTypeIn(typeIn);
            filter.setKSql("(or description ~ \'" + kSql + " \' name ~ \'" + kSql + " \')");

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
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
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
            PhoenixApiManager.execute(requestBuilder);
            clearDebugView();
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
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
