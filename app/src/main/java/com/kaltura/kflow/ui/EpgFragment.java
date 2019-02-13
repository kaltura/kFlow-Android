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
import com.kaltura.client.types.SearchAssetFilter;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.ui.debug.DebugFragment;
import com.kaltura.kflow.ui.main.MainActivity;
import com.kaltura.kflow.ui.assetList.AssetListFragment;
import com.kaltura.kflow.utils.ApiHelper;
import com.kaltura.kflow.utils.Utils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
public class EpgFragment extends DebugFragment implements View.OnClickListener {

    private TextInputEditText mEpgChannelId;
    private AppCompatButton mShowChannelButton;
    private ArrayList<Asset> mChannels = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_epg, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("EPG past programs");

        mEpgChannelId = getView().findViewById(R.id.epg_channel_id);
        mShowChannelButton = getView().findViewById(R.id.show_channel);

        mShowChannelButton.setOnClickListener(this);
        getView().findViewById(R.id.get).setOnClickListener(this);

        mEpgChannelId.setText("3286");
        mShowChannelButton.setVisibility(mChannels.isEmpty() ? View.GONE : View.VISIBLE);
        mShowChannelButton.setText(getResources().getQuantityString(R.plurals.show_past_programs,
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
                makeGetChannelsRequest(mEpgChannelId.getText().toString());
                break;
            }
        }
    }

    private void makeGetChannelsRequest(String epgChannelId) {
        if (Utils.hasInternetConnection(requireContext())) {

            mChannels.clear();
            mShowChannelButton.setVisibility(View.GONE);

            Calendar startCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            startCalendar.set(Calendar.MILLISECOND, 0);
            startCalendar.set(Calendar.SECOND, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.HOUR, 0);
            long startDate = startCalendar.getTimeInMillis() / 1000;
            long endDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis() / 1000;

            SearchAssetFilter filter = new SearchAssetFilter();
            filter.setOrderBy(AssetOrderBy.START_DATE_DESC.getValue());
            filter.setTypeIn("0");
            filter.setKSql("(and epg_channel_id = '" + epgChannelId + "' (and start_date < '" + endDate + "' end_date > '" + startDate + "' end_date < '" + endDate + "'))");

            FilterPager filterPager = new FilterPager();
            filterPager.setPageIndex(1);
            filterPager.setPageSize(10);

            RequestBuilder requestBuilder = AssetService.list(filter, filterPager)
                    .setCompletion(result -> {
                        if (result.isSuccess()) {
                            if (result.results.getObjects() != null)
                                mChannels.addAll(result.results.getObjects());

                            mShowChannelButton.setText(getResources().getQuantityString(R.plurals.show_past_programs,
                                    mChannels.size(), NumberFormat.getInstance().format(mChannels.size())));
                            mShowChannelButton.setVisibility(View.VISIBLE);
                        }
                    });
            ApiHelper.execute(requestBuilder);
            clearDebugView();
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
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
        ApiHelper.cancelAll();
    }

    @Override
    protected int getDebugViewId() {
        return R.id.debug_view;
    }
}
