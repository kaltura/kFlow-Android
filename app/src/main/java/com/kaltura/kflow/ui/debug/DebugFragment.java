package com.kaltura.kflow.ui.debug;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.kaltura.kflow.R;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.utils.Utils;

import org.json.JSONObject;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by alex_lytvynenko on 20.11.2018.
 */
public abstract class DebugFragment extends Fragment implements DebugListener {

    private DebugView mDebugView;
    private MenuItem mShareMenuItem;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_menu_items, menu);
        mShareMenuItem = menu.findItem(R.id.fragment_menu_share);
        mShareMenuItem.setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.fragment_menu_share) {
            share();
        }
        return super.onOptionsItemSelected(item);
    }

    private void share() {
        File file = Utils.saveToFile(requireContext(), mDebugView.getSharedData());
        Utils.shareFile(requireActivity(), file);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PhoenixApiManager.setDebugListener(this);

        mDebugView = getView().findViewById(getDebugViewId());

        if (mDebugView == null) throw new IllegalStateException("DebugView is not provided!");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PhoenixApiManager.removeDebugListener();
    }

    @Override
    public void setRequestInfo(String url, String method, int code) {
        mDebugView.setRequestUrl(url);
        mDebugView.setRequestMethod(method);
        mDebugView.setResponseCode(code);
    }

    @Override
    public void setRequestBody(JSONObject jsonObject) {
        mShareMenuItem.setEnabled(true);
        mDebugView.setRequestBody(jsonObject);
    }

    @Override
    public void setResponseBody(JSONObject jsonObject) {
        mDebugView.setResponseBody(jsonObject);
    }

    @Override
    public void onError() {
        mDebugView.onUnknownError();
        mShareMenuItem.setEnabled(false);
    }

    protected abstract int getDebugViewId();

    protected void clearDebugView() {
        mDebugView.clear();
        if (mShareMenuItem != null) mShareMenuItem.setEnabled(false);
    }
}
