package com.kaltura.kflow.presentation.productPrice;

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

import com.google.android.material.textfield.TextInputEditText;
import com.kaltura.client.enums.AssetReferenceType;
import com.kaltura.client.services.AssetService;
import com.kaltura.client.services.ProductPriceService;
import com.kaltura.client.types.Asset;
import com.kaltura.client.types.MediaFile;
import com.kaltura.client.types.ProductPrice;
import com.kaltura.client.types.ProductPriceFilter;
import com.kaltura.client.types.SubscriptionPrice;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.presentation.assetList.AssetListFragment;
import com.kaltura.kflow.presentation.debug.DebugFragment;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.utils.Utils;

import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by alex_lytvynenko on 27.11.2018.
 */
public class ProductPriceFragment extends DebugFragment implements ProductPriceListAdapter.OnProductPriceClickListener, View.OnClickListener {

    private TextInputEditText mAssetId;
    private AppCompatButton mShowProductPricesButton;
    private RecyclerView mProductPriceList;
    private ArrayList<ProductPrice> mProductPrices = new ArrayList<>();
    private ProductPriceListAdapter mProductPriceAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_price, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Product price");

        mAssetId = getView().findViewById(R.id.asset_id);
        mShowProductPricesButton = getView().findViewById(R.id.show_product_prices);
        initList();

        mShowProductPricesButton.setOnClickListener(this);
        getView().findViewById(R.id.get).setOnClickListener(this);

        mAssetId.setText("428755");

        mShowProductPricesButton.setVisibility(mProductPrices.isEmpty() ? View.GONE : View.VISIBLE);
        mShowProductPricesButton.setText(getResources().getQuantityString(R.plurals.show_product_prices,
                mProductPrices.size(), NumberFormat.getInstance().format(mProductPrices.size())));
    }

    private void initList() {
        mProductPriceList = getView().findViewById(R.id.product_price_list);
        mProductPriceList.setNestedScrollingEnabled(false);
        mProductPriceList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mProductPriceList.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));
        mProductPriceAdapter = new ProductPriceListAdapter(new ArrayList<>(), this);
        mProductPriceList.setAdapter(mProductPriceAdapter);
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());

        switch (view.getId()) {
            case R.id.show_product_prices: {
                showProductPrices();
                break;
            }
            case R.id.get: {
                makeGetAssetRequest(mAssetId.getText().toString());
                break;
            }
        }
    }

    private void makeGetAssetRequest(String assetId) {
        if (Utils.hasInternetConnection(requireContext())) {

            mProductPrices.clear();
            mShowProductPricesButton.setVisibility(View.GONE);
            mProductPriceList.setVisibility(View.GONE);

            RequestBuilder requestBuilder = AssetService.get(assetId, AssetReferenceType.MEDIA)
                    .setCompletion(result -> {
                        if (result.isSuccess()) makeGetProductPricesRequest(result.results);
                    });
            clearDebugView();
            PhoenixApiManager.execute(requestBuilder);
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeGetProductPricesRequest(Asset asset) {
        if (Utils.hasInternetConnection(requireContext())) {

            ProductPriceFilter productPriceFilter = new ProductPriceFilter();
            StringBuilder fileIdIn = new StringBuilder();
            if (asset.getMediaFiles() != null) {
                for (MediaFile mediaFile : asset.getMediaFiles()) {
                    if (asset.getMediaFiles().indexOf(mediaFile) != 0)
                        fileIdIn.append(", ");
                    fileIdIn.append(mediaFile.getId().toString());
                }
            }
            productPriceFilter.setFileIdIn(fileIdIn.toString());

            RequestBuilder requestBuilder = ProductPriceService.list(productPriceFilter)
                    .setCompletion(result -> {
                        if (result.isSuccess() && result.results != null) {
                            if (result.results.getObjects() != null)
                                mProductPrices.addAll(result.results.getObjects());

                            mShowProductPricesButton.setText(getResources().getQuantityString(R.plurals.show_product_prices,
                                    mProductPrices.size(), NumberFormat.getInstance().format(mProductPrices.size())));
                            mShowProductPricesButton.setVisibility(View.VISIBLE);
                        }
                    });
            clearDebugView();
            PhoenixApiManager.execute(requestBuilder);
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProductPrices() {
        mProductPriceList.setVisibility(View.VISIBLE);
        mShowProductPricesButton.setVisibility(View.GONE);
        mProductPriceAdapter = new ProductPriceListAdapter(mProductPrices, this);
        mProductPriceList.setAdapter(mProductPriceAdapter);
    }

    private void showAssets(ArrayList<Asset> assetList) {
        AssetListFragment assetListFragment = AssetListFragment.newInstance(assetList);
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

    @Override
    public void onSubscriptionPriceClicked(SubscriptionPrice subscriptionPrice) {

    }
}
