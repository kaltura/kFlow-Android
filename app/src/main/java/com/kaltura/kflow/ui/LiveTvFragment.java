package com.kaltura.kflow.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.kaltura.client.enums.AssetReferenceType;
import com.kaltura.client.services.AssetService;
import com.kaltura.client.types.Asset;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.ui.debug.DebugFragment;
import com.kaltura.kflow.ui.main.MainActivity;
import com.kaltura.kflow.ui.player.PlayerFragment;
import com.kaltura.kflow.utils.ApiHelper;
import com.kaltura.kflow.utils.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

/**
 * Created by alex_lytvynenko on 17.01.2019.
 */
public class LiveTvFragment extends DebugFragment implements View.OnClickListener {

    private TextInputEditText mChannelId;
    private AppCompatButton mShowChannelButton;
    private Asset mChannel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_live, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Live TV");

        mChannelId = getView().findViewById(R.id.channel_id);
        mShowChannelButton = getView().findViewById(R.id.show_channel);

        mShowChannelButton.setOnClickListener(this);
        getView().findViewById(R.id.get).setOnClickListener(this);

        mChannelId.setText("317163");
        mShowChannelButton.setVisibility(mChannel == null ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());

        switch (view.getId()) {
            case R.id.show_channel: {
                showChannel();
                break;
            }
            case R.id.get: {
                makeGetVodRequest(mChannelId.getText().toString());
                break;
            }
        }
    }

    private void makeGetVodRequest(String channelId) {
        if (Utils.hasInternetConnection(requireContext())) {

            mChannel = null;
            mShowChannelButton.setVisibility(View.GONE);

            RequestBuilder requestBuilder = AssetService.get(channelId, AssetReferenceType.MEDIA)
                    .setCompletion(result -> {
                        if (result.isSuccess()) {
                            mChannel = result.results;
                            mShowChannelButton.setVisibility(View.VISIBLE);
                        }
                    });
            ApiHelper.execute(requestBuilder);
            clearDebugView();
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChannel() {
        PlayerFragment playerFragment = PlayerFragment.newInstance(mChannel);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, playerFragment)
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
