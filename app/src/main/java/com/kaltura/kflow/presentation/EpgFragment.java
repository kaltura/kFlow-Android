package com.kaltura.kflow.presentation;

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
import com.kaltura.kflow.presentation.debug.DebugFragment;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.presentation.assetList.AssetListFragment;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.utils.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
public class EpgFragment extends DebugFragment implements View.OnClickListener {

    private static final int YESTERDAY = 0;
    private static final int TODAY = 1;
    private static final int TOMORROW = 2;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({YESTERDAY, TODAY, TOMORROW})
    @interface DateFilter {
    }

    private TextInputEditText mLinearMediaId;
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
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("EPG");

        mLinearMediaId = getView().findViewById(R.id.linear_media_id);
        mShowChannelButton = getView().findViewById(R.id.show_channel);

        mShowChannelButton.setOnClickListener(this);
        getView().findViewById(R.id.yesterday).setOnClickListener(this);
        getView().findViewById(R.id.today).setOnClickListener(this);
        getView().findViewById(R.id.tomorrow).setOnClickListener(this);

        mShowChannelButton.setVisibility(mChannels.isEmpty() ? View.GONE : View.VISIBLE);
        mShowChannelButton.setText(getResources().getQuantityString(R.plurals.show_programs,
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
            case R.id.yesterday: {
                makeGetChannelsRequest(mLinearMediaId.getText().toString(), YESTERDAY);
                break;
            }
            case R.id.today: {
                makeGetChannelsRequest(mLinearMediaId.getText().toString(), TODAY);
                break;
            }
            case R.id.tomorrow: {
                makeGetChannelsRequest(mLinearMediaId.getText().toString(), TOMORROW);
                break;
            }
        }
    }

    private void makeGetChannelsRequest(String epgChannelId, @DateFilter int dateFilter) {
        if (Utils.hasInternetConnection(requireContext())) {

            mChannels.clear();
            mShowChannelButton.setVisibility(View.GONE);

            long startDate = 0L;
            long endDate = 0L;

            Calendar todayMidnightCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            todayMidnightCalendar.set(Calendar.MILLISECOND, 0);
            todayMidnightCalendar.set(Calendar.SECOND, 0);
            todayMidnightCalendar.set(Calendar.MINUTE, 0);
            todayMidnightCalendar.set(Calendar.HOUR_OF_DAY, 0);

            switch (dateFilter) {
                case YESTERDAY: {
                    endDate = todayMidnightCalendar.getTimeInMillis() / 1000;
                    todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, -1);
                    startDate = todayMidnightCalendar.getTimeInMillis() / 1000;
                    break;
                }
                case TODAY: {
                    startDate = todayMidnightCalendar.getTimeInMillis() / 1000;
                    todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, 1);
                    endDate = todayMidnightCalendar.getTimeInMillis() / 1000;
                    break;
                }
                case TOMORROW: {
                    todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, 1);
                    startDate = todayMidnightCalendar.getTimeInMillis() / 1000;
                    todayMidnightCalendar.add(Calendar.DAY_OF_YEAR, 1);
                    endDate = todayMidnightCalendar.getTimeInMillis() / 1000;
                    break;
                }
            }

            SearchAssetFilter filter = new SearchAssetFilter();
            filter.setOrderBy(AssetOrderBy.START_DATE_DESC.getValue());
            filter.setTypeIn("0");
            filter.setKSql("(and linear_media_id = '" + epgChannelId + "' (and start_date > '" + startDate + "' end_date < '" + endDate + "'))");

            FilterPager filterPager = new FilterPager();
            filterPager.setPageSize(100);
            filterPager.setPageIndex(1);

            RequestBuilder requestBuilder = AssetService.list(filter, filterPager)
                    .setCompletion(result -> {
                        if (result.isSuccess()) {
                            if (result.results.getObjects() != null)
                                mChannels.addAll(result.results.getObjects());

                            mShowChannelButton.setText(getResources().getQuantityString(R.plurals.show_programs,
                                    mChannels.size(), NumberFormat.getInstance().format(mChannels.size())));
                            mShowChannelButton.setVisibility(View.VISIBLE);
                        }
                    });
            clearDebugView();
            PhoenixApiManager.execute(requestBuilder);
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChannels() {
        AssetListFragment assetListFragment = AssetListFragment.newInstance(mChannels, true);
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
