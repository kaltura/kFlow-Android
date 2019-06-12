package com.kaltura.kflow.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.kaltura.client.enums.AssetOrderBy;
import com.kaltura.client.services.AssetService;
import com.kaltura.client.types.Asset;
import com.kaltura.client.types.FilterPager;
import com.kaltura.client.types.ListResponse;
import com.kaltura.client.types.SearchAssetFilter;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.client.utils.response.base.ApiCompletion;
import com.kaltura.kflow.R;
import com.kaltura.kflow.ui.debug.DebugFragment;
import com.kaltura.kflow.ui.main.MainActivity;
import com.kaltura.kflow.ui.assetList.AssetListFragment;
import com.kaltura.kflow.utils.ApiHelper;
import com.kaltura.kflow.utils.Utils;

import java.text.NumberFormat;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
public class GetVodFragment extends DebugFragment implements View.OnClickListener {

    private TextInputEditText mKsqlRequest;
    private AppCompatButton mShowAssetsButton;
    private ArrayList<Asset> mAssets = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vod, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("VOD");

        mKsqlRequest = getView().findViewById(R.id.ksql_request);
        mShowAssetsButton = getView().findViewById(R.id.show_assets);

        mShowAssetsButton.setOnClickListener(this);
        getView().findViewById(R.id.get).setOnClickListener(this);

        mKsqlRequest.setText("(or name~\'Bigg Boss S12\')");
        mShowAssetsButton.setVisibility(mAssets.isEmpty() ? View.GONE : View.VISIBLE);
        mShowAssetsButton.setText(getResources().getQuantityString(R.plurals.show_assets,
                mAssets.size(), NumberFormat.getInstance().format(mAssets.size())));
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());

        switch (view.getId()) {
            case R.id.show_assets: {
                showAssets();
                break;
            }
            case R.id.get: {
                makeGetVodRequest(mKsqlRequest.getText().toString());
                break;
            }
        }
    }

    private void makeGetVodRequest(String kSqlRequest) {
        if (Utils.hasInternetConnection(requireContext())) {

            mAssets.clear();
            mShowAssetsButton.setVisibility(View.GONE);

            SearchAssetFilter filter = new SearchAssetFilter();
            filter.setOrderBy(AssetOrderBy.START_DATE_DESC.getValue());
            filter.setKSql(kSqlRequest);

            FilterPager filterPager = new FilterPager();
            filterPager.setPageIndex(1);
            filterPager.setPageSize(50);

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
            ApiHelper.execute(requestBuilder);
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
        ApiHelper.cancelAll();
    }

    @Override
    protected int getDebugViewId() {
        return R.id.debug_view;
    }
}
