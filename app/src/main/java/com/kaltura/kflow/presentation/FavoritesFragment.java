package com.kaltura.kflow.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.kaltura.client.services.AssetService;
import com.kaltura.client.services.FavoriteService;
import com.kaltura.client.types.Asset;
import com.kaltura.client.types.Favorite;
import com.kaltura.client.types.FilterPager;
import com.kaltura.client.types.SearchAssetFilter;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.presentation.assetList.AssetListFragment;
import com.kaltura.kflow.presentation.debug.DebugFragment;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.utils.Utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
public class FavoritesFragment extends DebugFragment implements View.OnClickListener {

    private AppCompatButton mShowAssetsButton;
    private ArrayList<Asset> mAssets = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Favorites");

        mShowAssetsButton = getView().findViewById(R.id.show_assets);

        mShowAssetsButton.setVisibility(mAssets.isEmpty() ? View.GONE : View.VISIBLE);
        mShowAssetsButton.setText(getResources().getQuantityString(R.plurals.show_assets,
                mAssets.size(), NumberFormat.getInstance().format(mAssets.size())));

        getView().findViewById(R.id.get_favorites).setOnClickListener(this);
        mShowAssetsButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.get_favorites: {
                getFavoritesRequest();
                break;
            }
            case R.id.show_assets: {
                showAssets();
                break;
            }
        }
        Utils.hideKeyboard(getView());
    }

    private void getFavoritesRequest() {
        if (Utils.hasInternetConnection(requireContext())) {

            mAssets.clear();
            mShowAssetsButton.setVisibility(View.GONE);

            RequestBuilder requestBuilder = FavoriteService.list()
                    .setCompletion(result -> {
                        if (result.isSuccess() && result.results.getObjects() != null) {
                            List<String> ids = new ArrayList<>();
                            for (Favorite favorite : result.results.getObjects())
                                ids.add(favorite.getAssetId().toString());

                            getAssetsByIds(ids);
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

    private void getAssetsByIds(List<String> ids) {
        if (ids.isEmpty()) {
            mShowAssetsButton.setText(getResources().getQuantityString(R.plurals.show_assets,
                    mAssets.size(), NumberFormat.getInstance().format(mAssets.size())));
            mShowAssetsButton.setVisibility(View.VISIBLE);
        } else {
            SearchAssetFilter filter = new SearchAssetFilter();
            StringBuilder kSql = new StringBuilder("(or");
            for (String id : ids)
                kSql.append(" media_id=\'").append(id).append("\'");
            kSql.append(")");

            filter.setKSql(kSql.toString());

            FilterPager filterPager = new FilterPager();
            filterPager.setPageSize(100);
            filterPager.setPageIndex(1);

            RequestBuilder requestBuilder = AssetService.list(filter, filterPager)
                    .setCompletion(result -> {
                        if (result.isSuccess() && result.results.getObjects() != null) {
                            mAssets.addAll(result.results.getObjects());

                            mShowAssetsButton.setText(getResources().getQuantityString(R.plurals.show_assets,
                                    mAssets.size(), NumberFormat.getInstance().format(mAssets.size())));
                            mShowAssetsButton.setVisibility(View.VISIBLE);
                        }
                    });
            PhoenixApiManager.execute(requestBuilder);
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
