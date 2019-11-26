package com.kaltura.kflow.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.kaltura.client.services.OttUserService;
import com.kaltura.client.types.OTTUser;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.client.utils.response.base.ApiCompletion;
import com.kaltura.kflow.R;
import com.kaltura.kflow.manager.PreferenceManager;
import com.kaltura.kflow.presentation.debug.DebugFragment;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.utils.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
public class RegistrationFragment extends DebugFragment implements View.OnClickListener {

    private TextInputEditText mFirstName;
    private TextInputEditText mLastName;
    private TextInputEditText mUserName;
    private TextInputEditText mEmail;
    private TextInputEditText mPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Registration");

        mFirstName = getView().findViewById(R.id.first_name);
        mLastName = getView().findViewById(R.id.last_name);
        mUserName = getView().findViewById(R.id.username);
        mEmail = getView().findViewById(R.id.email);
        mPassword = getView().findViewById(R.id.password);
        getView().findViewById(R.id.register).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());
        makeRegistrationRequest(mFirstName.getText().toString(), mLastName.getText().toString(),
                mUserName.getText().toString(), mEmail.getText().toString(), mPassword.getText().toString());
    }

    private void makeRegistrationRequest(String firstName, String lastName, String userName, String email, String password) {
        if (Utils.hasInternetConnection(requireContext())) {
            OTTUser ottUser = new OTTUser();
            ottUser.firstName(firstName);
            ottUser.lastName(lastName);
            ottUser.username(userName);
            ottUser.email(email);
            RequestBuilder requestBuilder = OttUserService.register(PreferenceManager.getInstance(requireContext()).getPartnerId(), ottUser, password)
                    .setCompletion((ApiCompletion<OTTUser>) result -> {

                    });
            clearDebugView();
            PhoenixApiManager.execute(requestBuilder);
        } else {
            Snackbar.make(getView(), "No Internet connection", Snackbar.LENGTH_LONG)
                    .setAction("Dismiss",view -> {})
                    .show();
        }
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
