package com.kaltura.kflow.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.kaltura.client.enums.AppTokenHashType;
import com.kaltura.client.services.AppTokenService;
import com.kaltura.client.services.OttUserService;
import com.kaltura.client.types.AppToken;
import com.kaltura.client.types.LoginSession;
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
public class AnonymousLoginFragment extends DebugFragment implements View.OnClickListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_anonymous_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Anonymous login");

        getView().findViewById(R.id.login).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());
        makeAnonymousLoginRequest();
    }

    private void makeAnonymousLoginRequest() {
        if (Utils.hasInternetConnection(requireContext())) {
            RequestBuilder requestBuilder = OttUserService.anonymousLogin(PreferenceManager.getInstance(requireContext()).getPartnerId(), Utils.getUUID(requireContext()))
                    .setCompletion((ApiCompletion<LoginSession>) result -> {
                        if (result.isSuccess()) {
                            PreferenceManager.getInstance(requireContext()).saveKs(result.results.getKs());
                            PhoenixApiManager.getClient().setKs(result.results.getKs());
//                                generateAppToken();
                        }
                    });
            clearDebugView();
            PhoenixApiManager.execute(requestBuilder);
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateAppToken() {
        AppToken appToken = new AppToken();
        appToken.setHashType(AppTokenHashType.SHA256);
        appToken.setSessionDuration(604800); // 604800 seconds = 7 days
        appToken.setExpiry(1832668157);

        RequestBuilder requestBuilder = AppTokenService.add(appToken)
                .setCompletion((ApiCompletion<AppToken>) result -> {

                });
        PhoenixApiManager.execute(requestBuilder);
        clearDebugView();
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
