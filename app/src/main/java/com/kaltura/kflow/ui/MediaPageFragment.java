package com.kaltura.kflow.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kaltura.client.enums.AssetReferenceType;
import com.kaltura.client.enums.AssetType;
import com.kaltura.client.enums.PinType;
import com.kaltura.client.enums.RuleType;
import com.kaltura.client.services.AssetService;
import com.kaltura.client.services.BookmarkService;
import com.kaltura.client.services.PinService;
import com.kaltura.client.services.ProductPriceService;
import com.kaltura.client.services.UserAssetRuleService;
import com.kaltura.client.types.Asset;
import com.kaltura.client.types.BookmarkFilter;
import com.kaltura.client.types.ListResponse;
import com.kaltura.client.types.ProductPriceFilter;
import com.kaltura.client.types.UserAssetRule;
import com.kaltura.client.types.UserAssetRuleFilter;
import com.kaltura.client.utils.request.MultiRequestBuilder;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.ui.debug.DebugFragment;
import com.kaltura.kflow.ui.main.MainActivity;
import com.kaltura.kflow.ui.player.PlayerFragment;
import com.kaltura.kflow.utils.Utils;

import java.util.List;

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
public class MediaPageFragment extends DebugFragment implements View.OnClickListener {

    private TextInputEditText mMediaId;
    private TextInputEditText mPin;
    private AppCompatButton mPlay;
    private AppCompatButton mGetProductPrice;
    private AppCompatButton mGetBookmark;
    private AppCompatButton mGetAssetRules;
    private AppCompatButton mCheckAll;
    private AppCompatButton mInsertPin;
    private LinearLayout mPinLayout;
    private TextInputLayout mPinInputLayout;
    private Asset mAsset;
    private int mParentalRuleId;

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
        mInsertPin = getView().findViewById(R.id.insert_pin);
        mPinLayout = getView().findViewById(R.id.pin_layout);
        mPin = getView().findViewById(R.id.pin);
        mPinInputLayout = getView().findViewById(R.id.pin_input_layout);

        mPlay.setOnClickListener(this);
        mGetProductPrice.setOnClickListener(this);
        mGetBookmark.setOnClickListener(this);
        mGetAssetRules.setOnClickListener(this);
        mCheckAll.setOnClickListener(this);
        mInsertPin.setOnClickListener(this);
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
                checkAllTogetherRequest(mMediaId.getText().toString());
                break;
            }
            case R.id.insert_pin: {
                if (mPinInputLayout.getVisibility() == View.GONE) {
                    showPinInput();
                } else {
                    checkPinRequest(mPin.getText().toString());
                }
                break;
            }
        }
    }

    private void getAssetRequest(String assetId) {
        if (Utils.hasInternetConnection(requireContext())) {

            mAsset = null;
            mParentalRuleId = 0;
            mPin.setText("");

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

                RequestBuilder requestBuilder = UserAssetRuleService.list(userAssetRuleFilter).setCompletion(result -> {
                    if (result.isSuccess()) {
                        if (result.results != null && result.results.getObjects() != null) {
                            handleUserRules(result.results.getObjects());
                        }
                        validateButtons();
                    }
                });
                PhoenixApiManager.execute(requestBuilder);
                clearDebugView();
            } else {
                Toast.makeText(requireContext(), "Wrong input", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAllTogetherRequest(String assetId) {
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

                multiRequestBuilder.setCompletion(result -> {
                    if (result.isSuccess()) {
                        if (result.results != null && result.results.get(2) != null) {
                            handleUserRules(((ListResponse) result.results.get(2)).getObjects());
                        }
                        validateButtons();
                    }
                });

                PhoenixApiManager.execute(multiRequestBuilder);
                clearDebugView();
            } else {
                Toast.makeText(requireContext(), "Wrong input", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPinRequest(String pin) {
        if (Utils.hasInternetConnection(requireContext())) {

            if (TextUtils.isDigitsOnly(pin)) {
                RequestBuilder requestBuilder = PinService.validate(pin, PinType.PARENTAL, mParentalRuleId);
                PhoenixApiManager.execute(requestBuilder);
                clearDebugView();
            } else {
                Toast.makeText(requireContext(), "Wrong input", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPinInput() {
        mPinInputLayout.setVisibility(View.VISIBLE);
        mInsertPin.setText("Check pin");
        Utils.showKeyboard(mPin);
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
        validatePinLayout();
    }

    private void validatePinLayout() {
        if (mParentalRuleId > 0) {
            mPinLayout.setVisibility(View.VISIBLE);
        } else {
            mPin.setText("");
            mPinLayout.setVisibility(View.GONE);
            mPinInputLayout.setVisibility(View.GONE);
            mInsertPin.setText("Insert pin");
        }
    }

    private void handleUserRules(List<UserAssetRule> userAssetRules) {
        for (UserAssetRule userAssetRule : userAssetRules) {
            if (userAssetRule.getRuleType() == RuleType.PARENTAL) {
                mParentalRuleId = userAssetRule.getId().intValue();
            }
        }
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
