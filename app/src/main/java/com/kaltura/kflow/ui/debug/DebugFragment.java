package com.kaltura.kflow.ui.debug;

import android.os.Bundle;
import android.view.View;

import com.kaltura.kflow.utils.ApiHelper;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Created by alex_lytvynenko on 20.11.2018.
 */
public abstract class DebugFragment extends Fragment implements DebugListener {

    private DebugView mDebugView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ApiHelper.setDebugListener(this);

        mDebugView = getView().findViewById(getDebugViewId());

        if (mDebugView == null) throw new IllegalStateException("DebugView is not provided!");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ApiHelper.removeDebugListener();
    }

    @Override
    public void setRequestBody(JSONObject jsonObject) {
        mDebugView.setRequestBody(jsonObject);
    }

    @Override
    public void setResponseBody(JSONObject jsonObject) {
        mDebugView.setResponseBody(jsonObject);
    }

    @Override
    public void onError() {
        mDebugView.onUnknownError();
    }

    protected abstract int getDebugViewId();

    protected void clearDebugView() {
        mDebugView.clear();
    }
}
