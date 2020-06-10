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
import com.kaltura.client.types.LiveAsset;
import com.kaltura.client.types.SearchAssetFilter;
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

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
public class LiveTvFragment extends DebugFragment implements View.OnClickListener {

    private TextInputEditText mChannelName;
    private AppCompatButton mShowChannelButton;
    private ArrayList<Asset> mChannels = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_live, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Live TV");

        mChannelName = getView().findViewById(R.id.channel_name);
        mShowChannelButton = getView().findViewById(R.id.show_channel);

        mShowChannelButton.setOnClickListener(this);
        getView().findViewById(R.id.get).setOnClickListener(this);

        mShowChannelButton.setVisibility(mChannels.isEmpty() ? View.GONE : View.VISIBLE);
        mShowChannelButton.setText(getResources().getQuantityString(R.plurals.show_channels,
                mChannels.size(), NumberFormat.getInstance().format(mChannels.size())));
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());

        switch (view.getId()) {
            case R.id.show_channel: {
                showChannels();
                break;
            }
            case R.id.get: {
                makeGetChannelsRequest(mChannelName.getText().toString());
                break;
            }
        }
    }

    private void makeGetChannelsRequest(String channelName) {
        if (Utils.hasInternetConnection(requireContext())) {

            mChannels.clear();
            mShowChannelButton.setVisibility(View.GONE);

            SearchAssetFilter filter = new SearchAssetFilter();
            filter.setOrderBy(AssetOrderBy.START_DATE_DESC.getValue());
            filter.setName(channelName);
            filter.setKSql("(and name~'" + channelName + "')");

            FilterPager filterPager = new FilterPager();
            filterPager.setPageIndex(1);
            filterPager.setPageSize(50);

            RequestBuilder requestBuilder = AssetService.list(filter, filterPager)
                    .setCompletion(result -> {
                        if (result.isSuccess()) {
                            if (result.results.getObjects() != null)
                                for (Asset asset : result.results.getObjects()) {
                                    if (asset instanceof LiveAsset)
                                        mChannels.add(asset);
                                }

                            mShowChannelButton.setText(getResources().getQuantityString(R.plurals.show_channels,
                                    mChannels.size(), NumberFormat.getInstance().format(mChannels.size())));
                            mShowChannelButton.setVisibility(View.VISIBLE);
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

    private void showChannels() {
        AssetListFragment assetListFragment = AssetListFragment.newInstance(mChannels);
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
