package com.kaltura.kflow.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.kaltura.client.services.OttUserService;
import com.kaltura.client.types.LoginResponse;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.client.utils.response.base.ApiCompletion;
import com.kaltura.kflow.R;
import com.kaltura.kflow.manager.PreferenceManager;
import com.kaltura.kflow.ui.debug.DebugFragment;
import com.kaltura.kflow.ui.main.MainActivity;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.utils.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
public class LoginFragment extends DebugFragment implements View.OnClickListener {

    private TextInputEditText mUsername;
    private TextInputEditText mPassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Login");

        mUsername = getView().findViewById(R.id.username);
        mPassword = getView().findViewById(R.id.password);
        getView().findViewById(R.id.login).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());
        makeLoginRequest(mUsername.getText().toString(), mPassword.getText().toString());
    }

    private void makeLoginRequest(String email, String password) {
        if (Utils.hasInternetConnection(requireContext())) {
            RequestBuilder requestBuilder = OttUserService.login(PreferenceManager.getInstance(requireContext()).getPartnerId(), email, password,
                    null, Utils.getUUID(requireContext()))
                    .setCompletion((ApiCompletion<LoginResponse>) result -> {
                        if (result.isSuccess()) {
                            PreferenceManager.getInstance(requireContext()).saveKs(result.results.getLoginSession().getKs());
                            PhoenixApiManager.getClient().setKs(result.results.getLoginSession().getKs());
                        }
                    });
            clearDebugView();
            PhoenixApiManager.execute(requestBuilder);
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
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
