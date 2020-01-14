package com.kaltura.kflow.presentation;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.kaltura.client.Configuration;
import com.kaltura.kflow.R;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.manager.PreferenceManager;
import com.kaltura.kflow.presentation.main.MainActivity;

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener {

    private TextInputEditText mUrl;
    private TextInputEditText mPartnerId;
    private TextInputEditText mMediaFileFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Settings");

        mUrl = getView().findViewById(R.id.url);
        mPartnerId = getView().findViewById(R.id.partner_id);
        mMediaFileFormat = getView().findViewById(R.id.media_file_format);

        getView().findViewById(R.id.save).setOnClickListener(this);
//        initUI();
        ApplyDialogConfiguration();
    }

    private void ApplyDialogConfiguration() {

        mUrl.setText("https://restv4-as.ott.kaltura.com/v5_0_3");
        mPartnerId.setText("3065");
        mMediaFileFormat.setText("Mobile_Dash_SD");

    }

    @Override
    public void onClick(View view) {
        save(mUrl.getText().toString(), mPartnerId.getText().toString(), mMediaFileFormat.getText().toString());
    }

    private void initUI() {
        mUrl.setText(PreferenceManager.getInstance(requireContext()).getBaseUrl());
        mPartnerId.setText(String.valueOf(PreferenceManager.getInstance(requireContext()).getPartnerId()));
        mMediaFileFormat.setText(PreferenceManager.getInstance(requireContext()).getMediaFileFormat());
    }

    private void save(String baseUrl, String partnerId, String mediaFileFormat) {
        if (!baseUrl.isEmpty()) {
            PreferenceManager.getInstance(requireContext()).clearKs();
            PreferenceManager.getInstance(requireContext()).saveBaseUrl(baseUrl);
            Configuration config = new Configuration();
            config.setEndpoint(PreferenceManager.getInstance(requireContext()).getBaseUrl());
            PhoenixApiManager.getClient().setConnectionConfiguration(config);
            PhoenixApiManager.getClient().setKs(null);
        }else{
            Toast.makeText(requireContext(), "END Point URL is empty", Toast.LENGTH_SHORT).show();
        }

        if (!partnerId.isEmpty() && TextUtils.isDigitsOnly(partnerId)) {
            PreferenceManager.getInstance(requireContext()).clearKs();
            PreferenceManager.getInstance(requireContext()).savePartnerId(Integer.parseInt(partnerId));
            PhoenixApiManager.getClient().setKs(null);
        }else {
            Toast.makeText(requireContext(), "Parthner ID is missing or invalid", Toast.LENGTH_SHORT).show();
        }

        if (!mediaFileFormat.isEmpty()) {
            PreferenceManager.getInstance(requireContext()).saveMediaFileFormat(mediaFileFormat);
        }else {
            Toast.makeText(requireContext(), "Media File Format is missing", Toast.LENGTH_SHORT).show();
        }
        Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show();
    }
}
