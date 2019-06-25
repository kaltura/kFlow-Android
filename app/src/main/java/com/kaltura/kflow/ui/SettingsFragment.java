package com.kaltura.kflow.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;

import com.kaltura.client.Configuration;
import com.kaltura.kflow.R;
import com.kaltura.kflow.entity.AccountEntity;
import com.kaltura.kflow.entity.PartnerEntity;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.manager.PreferenceManager;
import com.kaltura.kflow.ui.main.MainActivity;
import com.kaltura.kflow.manager.ConfigurationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
public class SettingsFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private AppCompatSpinner mAccountSpinner;
    private AppCompatSpinner mPartnerSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Settings");

        mAccountSpinner = getView().findViewById(R.id.account_spinner);
        mPartnerSpinner = getView().findViewById(R.id.partner_id_spinner);

        mAccountSpinner.setOnItemSelectedListener(this);
        mPartnerSpinner.setOnItemSelectedListener(this);
        getView().findViewById(R.id.save).setOnClickListener(this);

        initAccountSpinner();
    }

    @Override
    public void onClick(View view) {
        save();
    }

    private void initAccountSpinner() {
        List<String> accountsName = new ArrayList<>();
        int selectedPosition = 0;
        for (AccountEntity accountEntity : ConfigurationManager.getInstance().getConfiguration().getAccounts()) {
            accountsName.add(accountEntity.getName());
            if (accountEntity.getBaseUrl().equals(PreferenceManager.getInstance(requireContext()).getBaseUrl())) {
                selectedPosition = ConfigurationManager.getInstance().getConfiguration().getAccounts().indexOf(accountEntity);
            }
        }
        mAccountSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, accountsName));
        mAccountSpinner.setSelection(selectedPosition);
    }

    private void initPartnerSpinner(AccountEntity accountEntity) {
        List<String> partnersName = new ArrayList<>();
        for (PartnerEntity partnerEntity : accountEntity.getPartners()) {
            partnersName.add(partnerEntity.getName());
        }
        mPartnerSpinner.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, partnersName));
    }

    private void save() {
        PreferenceManager.getInstance(requireContext()).clear();
        AccountEntity accountEntity = ConfigurationManager.getInstance().getConfiguration().getAccounts().get(mAccountSpinner.getSelectedItemPosition());
        PartnerEntity partnerEntity = accountEntity.getPartners().get(mPartnerSpinner.getSelectedItemPosition());
        PreferenceManager.getInstance(requireContext()).saveBaseUrl(accountEntity.getBaseUrl());
        PreferenceManager.getInstance(requireContext()).savePartnerId(Integer.parseInt(partnerEntity.getPartnerId()));

        Configuration config = new Configuration();
        config.setEndpoint(PreferenceManager.getInstance(requireContext()).getBaseUrl());
        PhoenixApiManager.getClient().setConnectionConfiguration(config);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.account_spinner) {
            initPartnerSpinner(ConfigurationManager.getInstance().getConfiguration().getAccounts().get(position));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}
