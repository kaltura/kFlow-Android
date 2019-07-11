package com.kaltura.kflow.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.textfield.TextInputEditText;
import com.kaltura.client.enums.AssetReferenceType;
import com.kaltura.client.enums.AssetType;
import com.kaltura.client.services.AssetService;
import com.kaltura.client.services.BookmarkService;
import com.kaltura.client.services.ProductPriceService;
import com.kaltura.client.services.UserAssetRuleService;
import com.kaltura.client.types.Asset;
import com.kaltura.client.types.BookmarkFilter;
import com.kaltura.client.types.ProductPriceFilter;
import com.kaltura.client.types.UserAssetRuleFilter;
import com.kaltura.client.utils.request.MultiRequestBuilder;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.ui.debug.DebugFragment;
import com.kaltura.kflow.ui.main.MainActivity;
import com.kaltura.kflow.ui.player.PlayerFragment;
import com.kaltura.kflow.utils.Utils;

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
public class MediaPageFragment extends DebugFragment implements View.OnClickListener {

    private TextInputEditText mMediaId;
    private AppCompatButton mPlay;
    private AppCompatButton mGetProductPrice;
    private AppCompatButton mGetBookmark;
    private AppCompatButton mGetAssetRules;
    private AppCompatButton mCheckAll;
    private Asset mAsset;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Media page");

        mMediaId = getView().findViewById(R.id.id);
        mPlay = getView().findViewById(R.id.play_asset);
        mGetProductPrice = getView().findViewById(R.id.get_product_price);
        mGetBookmark = getView().findViewById(R.id.get_bookmark);
        mGetAssetRules = getView().findViewById(R.id.get_asset_rules);
        mCheckAll = getView().findViewById(R.id.check_all);

        mPlay.setOnClickListener(this);
        mGetProductPrice.setOnClickListener(this);
        mGetBookmark.setOnClickListener(this);
        mGetAssetRules.setOnClickListener(this);
        mCheckAll.setOnClickListener(this);
        getView().findViewById(R.id.get).setOnClickListener(this);

        validateButtons();
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());

        switch (view.getId()) {
            case R.id.play_asset: {
                playAsset();
                break;
            }
            case R.id.get: {
                getAssetRequest(mMediaId.getText().toString());
                break;
            }
            case R.id.get_product_price: {
                getProductPriceRequest(mMediaId.getText().toString());
                break;
            }
            case R.id.get_bookmark: {
                getBookmarkRequest(mMediaId.getText().toString());
                break;
            }
            case R.id.get_asset_rules: {
                getAssetRulesRequest(mMediaId.getText().toString());
                break;
            }
            case R.id.check_all: {
                checkAllTogether(mMediaId.getText().toString());
                break;
            }
        }
    }

    private void getAssetRequest(String assetId) {
        if (Utils.hasInternetConnection(requireContext())) {

            mAsset = null;
            validateButtons();

            RequestBuilder requestBuilder = AssetService.get(assetId, AssetReferenceType.MEDIA)
                    .setCompletion(result -> {
                        if (result.isSuccess()) {
                            if (result.results != null) mAsset = result.results;
                            validateButtons();
                        }
                    });
            PhoenixApiManager.execute(requestBuilder);
            clearDebugView();
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getProductPriceRequest(String assetId) {
        if (Utils.hasInternetConnection(requireContext())) {

            ProductPriceFilter productPriceFilter = new ProductPriceFilter();
            productPriceFilter.setFileIdIn(assetId);

            RequestBuilder requestBuilder = ProductPriceService.list(productPriceFilter);
            PhoenixApiManager.execute(requestBuilder);
            clearDebugView();
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getBookmarkRequest(String assetId) {
        if (Utils.hasInternetConnection(requireContext())) {

            BookmarkFilter bookmarkFilter = new BookmarkFilter();
            bookmarkFilter.setAssetIdIn(assetId);
            bookmarkFilter.setAssetTypeEqual(AssetType.MEDIA);

            RequestBuilder requestBuilder = BookmarkService.list(bookmarkFilter);
            PhoenixApiManager.execute(requestBuilder);
            clearDebugView();
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getAssetRulesRequest(String assetId) {
        if (Utils.hasInternetConnection(requireContext())) {

            if (TextUtils.isDigitsOnly(assetId)) {
                UserAssetRuleFilter userAssetRuleFilter = new UserAssetRuleFilter();
                userAssetRuleFilter.setAssetTypeEqual(1);
                userAssetRuleFilter.setAssetIdEqual(Long.parseLong(assetId));

                RequestBuilder requestBuilder = UserAssetRuleService.list(userAssetRuleFilter);
                PhoenixApiManager.execute(requestBuilder);
                clearDebugView();
            } else {
                Toast.makeText(requireContext(), "Wrong input", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAllTogether(String assetId) {
        if (Utils.hasInternetConnection(requireContext())) {

            if (TextUtils.isDigitsOnly(assetId)) {
                MultiRequestBuilder multiRequestBuilder = new MultiRequestBuilder();

                // product price request
                ProductPriceFilter productPriceFilter = new ProductPriceFilter();
                productPriceFilter.setFileIdIn(assetId);
                multiRequestBuilder.add(ProductPriceService.list(productPriceFilter));

                // bookmark request
                BookmarkFilter bookmarkFilter = new BookmarkFilter();
                bookmarkFilter.setAssetIdIn(assetId);
                bookmarkFilter.setAssetTypeEqual(AssetType.MEDIA);
                multiRequestBuilder.add(BookmarkService.list(bookmarkFilter));

                // asset rules request
                UserAssetRuleFilter userAssetRuleFilter = new UserAssetRuleFilter();
                userAssetRuleFilter.setAssetTypeEqual(1);
                userAssetRuleFilter.setAssetIdEqual(Long.parseLong(assetId));
                multiRequestBuilder.add(UserAssetRuleService.list(userAssetRuleFilter));

                PhoenixApiManager.execute(multiRequestBuilder);
                clearDebugView();
            } else {
                Toast.makeText(requireContext(), "Wrong input", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void playAsset() {
        PlayerFragment assetListFragment = PlayerFragment.newInstance(mAsset);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, assetListFragment)
                .addToBackStack(null)
                .commit();
    }

    private void validateButtons() {
        int visibility = mAsset == null ? View.GONE : View.VISIBLE;
        mPlay.setVisibility(visibility);
        mGetProductPrice.setVisibility(visibility);
        mGetBookmark.setVisibility(visibility);
        mGetAssetRules.setVisibility(visibility);
        mCheckAll.setVisibility(visibility);
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
