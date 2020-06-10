package com.kaltura.kflow.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kaltura.client.enums.AssetOrderBy;
import com.kaltura.client.services.AssetService;
import com.kaltura.client.types.Asset;
import com.kaltura.client.types.FilterPager;
import com.kaltura.client.types.ListResponse;
import com.kaltura.client.types.MediaAsset;
import com.kaltura.client.types.SearchAssetFilter;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.client.utils.response.base.ApiCompletion;
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

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
public class GetVodFragment extends DebugFragment implements View.OnClickListener {

    private TextInputEditText mNameRequest;
    private TextInputEditText mTypeRequest;
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

        mNameRequest = getView().findViewById(R.id.name_request);
        mTypeRequest = getView().findViewById(R.id.type_request);
        mShowAssetsButton = getView().findViewById(R.id.show_assets);

        mShowAssetsButton.setOnClickListener(this);
        getView().findViewById(R.id.get).setOnClickListener(this);

        mTypeRequest.setText("1088, 1089, 1091");
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
                makeGetVodRequest(mNameRequest.getText().toString(), mTypeRequest.getText().toString());
                break;
            }
        }
    }

    private void makeGetVodRequest(String name, String type) {
        if (Utils.hasInternetConnection(requireContext())) {

            mAssets.clear();
            mShowAssetsButton.setVisibility(View.GONE);

            ArrayList<String> assetTypes = new ArrayList<>();
            String[] types = type.split(",");
            for (String t : types) {
                if (!t.trim().isEmpty()) {
                    assetTypes.add(t.trim());
                }
            }

            StringBuilder kSql = new StringBuilder("(or name~\'" + name + "\'");
            for (String t : assetTypes) {
                kSql.append(" asset_type=\'").append(t).append("\'");
            }
            kSql.append(")");

            SearchAssetFilter filter = new SearchAssetFilter();
            filter.setOrderBy(AssetOrderBy.START_DATE_DESC.getValue());
            filter.setKSql(kSql.toString());

            FilterPager filterPager = new FilterPager();
            filterPager.setPageIndex(1);
            filterPager.setPageSize(50);

            RequestBuilder requestBuilder = AssetService.list(filter, filterPager)
                    .setCompletion((ApiCompletion<ListResponse<Asset>>) result -> {
                        if (result.isSuccess()) {
                            if (result.results.getObjects() != null)
                                for (Asset asset : result.results.getObjects()) {
                                    if (asset instanceof MediaAsset)
                                        mAssets.add(asset);
                                }

                            mShowAssetsButton.setText(getResources().getQuantityString(R.plurals.show_assets,
                                    mAssets.size(), NumberFormat.getInstance().format(mAssets.size())));
                            mShowAssetsButton.setVisibility(View.VISIBLE);
                        }
                    });
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
