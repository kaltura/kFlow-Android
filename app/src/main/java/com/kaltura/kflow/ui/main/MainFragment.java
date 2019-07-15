package com.kaltura.kflow.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaltura.kflow.R;
import com.kaltura.kflow.ui.AnonymousLoginFragment;
import com.kaltura.kflow.ui.EpgFragment;
import com.kaltura.kflow.ui.FavoritesFragment;
import com.kaltura.kflow.ui.LiveTvFragment;
import com.kaltura.kflow.ui.LoginFragment;
import com.kaltura.kflow.ui.MediaPageFragment;
import com.kaltura.kflow.ui.RegistrationFragment;
import com.kaltura.kflow.ui.GetVodFragment;
import com.kaltura.kflow.ui.SearchFragment;
import com.kaltura.kflow.ui.SettingsFragment;
import com.kaltura.kflow.ui.SubscriptionFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.kaltura.kflow.ui.main.Feature.ANONYMOUS_LOGIN;
import static com.kaltura.kflow.ui.main.Feature.EPG;
import static com.kaltura.kflow.ui.main.Feature.FAVORITES;
import static com.kaltura.kflow.ui.main.Feature.LIVE;
import static com.kaltura.kflow.ui.main.Feature.LOGIN;
import static com.kaltura.kflow.ui.main.Feature.MEDIA_PAGE;
import static com.kaltura.kflow.ui.main.Feature.REGISTRATION;
import static com.kaltura.kflow.ui.main.Feature.SEARCH;
import static com.kaltura.kflow.ui.main.Feature.SETTINGS;
import static com.kaltura.kflow.ui.main.Feature.SUBSCRIPTION;
import static com.kaltura.kflow.ui.main.Feature.VOD;

/**
 * Created by alex_lytvynenko on 11/18/18.
 */
public class MainFragment extends Fragment implements FeatureAdapter.OnFeatureClickListener {

    private Feature[] mFeatures = {LOGIN, ANONYMOUS_LOGIN, REGISTRATION, VOD, EPG, LIVE, FAVORITES,
            SEARCH, MEDIA_PAGE, SUBSCRIPTION, SETTINGS};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initList();
    }

    private void initList() {
        RecyclerView list = getView().findViewById(R.id.list);
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));
        FeatureAdapter adapter = new FeatureAdapter(mFeatures, this);
        list.setAdapter(adapter);
    }

    @Override
    public void onFeatureClicked(Feature feature) {
        switch (feature) {
            case LOGIN:
                pushFragment(new LoginFragment());
                break;
            case ANONYMOUS_LOGIN:
                pushFragment(new AnonymousLoginFragment());
                break;
            case REGISTRATION:
                pushFragment(new RegistrationFragment());
                break;
            case VOD:
                pushFragment(new GetVodFragment());
                break;
            case EPG:
                pushFragment(new EpgFragment());
                break;
            case LIVE:
                pushFragment(new LiveTvFragment());
                break;
            case FAVORITES:
                pushFragment(new FavoritesFragment());
                break;
            case SEARCH:
                pushFragment(new SearchFragment());
                break;
            case MEDIA_PAGE:
                pushFragment(new MediaPageFragment());
                break;
            case SUBSCRIPTION:
                pushFragment(new SubscriptionFragment());
                break;
            case SETTINGS:
                pushFragment(new SettingsFragment());
                break;
        }
    }

    private void pushFragment(Fragment fragment) {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
