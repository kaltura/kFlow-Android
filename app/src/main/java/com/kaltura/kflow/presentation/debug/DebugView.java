package com.kaltura.kflow.presentation.debug;

import android.content.Context;
import android.graphics.PorterDuff;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.kaltura.kflow.R;

import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by alex_lytvynenko on 20.11.2018.
 */
public class DebugView extends RelativeLayout {

    private AppCompatTextView mRequestBodyText;
    private AppCompatTextView mResponseBodyText;
    private AppCompatImageView mRequestBodySort;
    private AppCompatImageView mResponseBodySort;
    private RelativeLayout mRequestContainer;
    private RelativeLayout mResponseContainer;

    private String mRequestUrl;
    private String mRequestMethod;
    private int mResponseCode;
    private JSONObject mRequestJson;
    private JSONObject mResponseJson;

    public DebugView(Context context) {
        super(context);
        init();
    }

    public DebugView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_debug, this);
        mRequestBodyText = view.findViewById(R.id.request_body);
        mResponseBodyText = view.findViewById(R.id.response_body);
        mRequestBodySort = view.findViewById(R.id.request_sort);
        mResponseBodySort = view.findViewById(R.id.response_sort);
        mRequestContainer = view.findViewById(R.id.request_container);
        mResponseContainer = view.findViewById(R.id.response_container);

        mRequestBodySort.setOnClickListener(view1 -> {
            if (mRequestBodySort.isSelected()) {
                mRequestBodySort.setSelected(false);
                mRequestBodySort.getDrawable().setColorFilter(null);
            } else {
                mRequestBodySort.setSelected(true);
                mRequestBodySort.getDrawable().mutate().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            }
            setRequestBody(mRequestJson);
        });

        mResponseBodySort.setOnClickListener(view12 -> {
            if (mResponseBodySort.isSelected()) {
                mResponseBodySort.setSelected(false);
                mResponseBodySort.getDrawable().setColorFilter(null);
            } else {
                mResponseBodySort.setSelected(true);
                mResponseBodySort.getDrawable().mutate().setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
            }
            setResponseBody(mResponseJson);
        });
    }

    public void setRequestUrl(String url) {
        mRequestUrl = url;
    }

    public void setRequestMethod(String requestMethod) {
        mRequestMethod = requestMethod;
    }

    public void setResponseCode(int responseCode) {
        mResponseCode = responseCode;
    }

    public void setRequestBody(JSONObject json) {
        try {
            mRequestJson = json;
            String text = mRequestBodySort.isSelected() ? json.toString(2) : json.toString();
            TransitionManager.beginDelayedTransition(mRequestContainer);
            mRequestBodyText.setText(text);
            mRequestBodySort.setVisibility(VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setResponseBody(JSONObject json) {
        try {
            mResponseJson = json;
            String text = mResponseBodySort.isSelected() ? json.toString(2) : json.toString();
            TransitionManager.beginDelayedTransition(mResponseContainer);
            mResponseBodyText.setText(text);
            mResponseBodySort.setVisibility(VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onUnknownError() {
        mRequestBodyText.setText("");
        mResponseBodyText.setText("Error!");
        mRequestBodySort.setVisibility(INVISIBLE);
        mResponseBodySort.setVisibility(INVISIBLE);
    }

    public void clear() {
        mRequestUrl = "";
        mRequestMethod = "";
        mResponseCode = -1;
        mRequestJson = null;
        mResponseJson = null;
        mRequestBodyText.setText("");
        mResponseBodyText.setText("");
        mRequestBodySort.setVisibility(INVISIBLE);
        mResponseBodySort.setVisibility(INVISIBLE);
    }

    public String getSharedData() {
        return "URL " + mRequestUrl + "\n" +
                "Method " + mRequestMethod + "\n" +
                "Status " + mResponseCode + "\n" +
                "Request Body:\n" + mRequestBodyText.getText() + "\n" +
                "Response Body:\n" + mResponseBodyText.getText() + "\n";
    }
}
