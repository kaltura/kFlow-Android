package com.kaltura.kflow.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import com.kaltura.client.enums.RecordingStatus;
import com.kaltura.client.services.RecordingService;
import com.kaltura.client.types.Recording;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.presentation.debug.DebugFragment;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.utils.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
public class RecordingsFragment extends DebugFragment implements View.OnClickListener {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RECORDED_FILTER, ON_GOING_FILTER, SCHEDULED_FILTER})
    @interface RecordingsFilter {
    }

    private static final int RECORDED_FILTER = 0;
    private static final int ON_GOING_FILTER = 1;
    private static final int SCHEDULED_FILTER = 2;

    private AppCompatButton mShowRecordings;
    private ArrayList<Recording> mAllRecordings = new ArrayList<>();
    private ArrayList<Recording> mFilteredRecordings = new ArrayList<>();
    private @RecordingsFilter
    int mRecordingFilter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recordings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Recordings");

        mShowRecordings = getView().findViewById(R.id.show_recordings);
        mShowRecordings.setOnClickListener(this);
        getView().findViewById(R.id.get_recorded).setOnClickListener(this);
        getView().findViewById(R.id.get_on_going).setOnClickListener(this);
        getView().findViewById(R.id.get_scheduled).setOnClickListener(this);

        mShowRecordings.setVisibility(mAllRecordings.isEmpty() ? View.GONE : View.VISIBLE);
        filterRecordings();
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());
        switch (view.getId()) {
            case R.id.show_recordings: {
                showRecordings();
                break;
            }
            case R.id.get_recorded: {
                mRecordingFilter = RECORDED_FILTER;
                getRecordingsRequest();
                break;
            }
            case R.id.get_on_going: {
                mRecordingFilter = ON_GOING_FILTER;
                getRecordingsRequest();
                break;
            }
            case R.id.get_scheduled: {
                mRecordingFilter = SCHEDULED_FILTER;
                getRecordingsRequest();
                break;
            }
        }
    }

    private void getRecordingsRequest() {
        if (mAllRecordings.isEmpty()) {
            if (Utils.hasInternetConnection(requireContext())) {
                mAllRecordings.clear();
                mFilteredRecordings.clear();
                mShowRecordings.setVisibility(View.GONE);

                RequestBuilder requestBuilder = RecordingService.list()
                        .setCompletion(result -> {
                            if (result.isSuccess()) {
                                mAllRecordings.addAll(result.results.getObjects());
                                filterRecordings();
                            }
                        });
                clearDebugView();
                PhoenixApiManager.execute(requestBuilder);
            } else {
                Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            filterRecordings();
        }
    }

    private void filterRecordings() {
        if (!mAllRecordings.isEmpty()) {
            mFilteredRecordings.clear();
            switch (mRecordingFilter) {
                case RECORDED_FILTER: {
                    for (Recording recording : mAllRecordings) {
                        if (recording.getStatus() == RecordingStatus.RECORDED)
                            mFilteredRecordings.add(recording);
                    }

                    if (mFilteredRecordings.isEmpty())
                        mShowRecordings.setText(R.string.show_empty_recorded);
                    else
                        mShowRecordings.setText(getResources().getQuantityString(R.plurals.show_recorded,
                                mFilteredRecordings.size(), NumberFormat.getInstance().format(mFilteredRecordings.size())));
                    break;
                }
                case ON_GOING_FILTER: {
                    for (Recording recording : mAllRecordings) {
                        if (recording.getStatus() == RecordingStatus.RECORDING)
                            mFilteredRecordings.add(recording);
                    }

                    if (mFilteredRecordings.isEmpty())
                        mShowRecordings.setText(R.string.show_empty_ongoing_recordings);
                    else
                        mShowRecordings.setText(getResources().getQuantityString(R.plurals.show_on_going_recording,
                                mFilteredRecordings.size(), NumberFormat.getInstance().format(mFilteredRecordings.size())));

                    break;
                }
                case SCHEDULED_FILTER: {
                    for (Recording recording : mAllRecordings) {
                        if (recording.getStatus() == RecordingStatus.SCHEDULED)
                            mFilteredRecordings.add(recording);
                    }

                    if (mFilteredRecordings.isEmpty())
                        mShowRecordings.setText(R.string.show_empty_scheduled_recordings);
                    else
                        mShowRecordings.setText(getResources().getQuantityString(R.plurals.show_scheduled_recording,
                                mFilteredRecordings.size(), NumberFormat.getInstance().format(mFilteredRecordings.size())));

                    break;
                }
            }
            mShowRecordings.setVisibility(View.VISIBLE);
        }
    }

    private void showRecordings() {
//        AssetListFragment assetListFragment = AssetListFragment.newInstance(mFilteredRecordings);
//        requireActivity().getSupportFragmentManager().beginTransaction()
//                .replace(R.id.container, assetListFragment)
//                .addToBackStack(null)
//                .commit();
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
