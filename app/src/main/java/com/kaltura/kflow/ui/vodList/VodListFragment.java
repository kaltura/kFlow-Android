package com.kaltura.kflow.ui.vodList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaltura.client.types.Asset;
import com.kaltura.kflow.R;
import com.kaltura.kflow.ui.player.PlayerFragment;
import com.kaltura.kflow.ui.main.MainActivity;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by alex_lytvynenko on 30.11.2018.
 */
public class VodListFragment extends Fragment implements VodListAdapter.OnAssetClickListener {

    private static final String ARG_ASSETS = "extra_assets";

    public static VodListFragment newInstance(ArrayList<Asset> assets) {
        VodListFragment vodListFragment = new VodListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ASSETS, assets);
        vodListFragment.setArguments(bundle);
        return vodListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vod_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("VOD list");

        initList();
    }

    private void initList() {
        RecyclerView list = getView().findViewById(R.id.list);
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

        Bundle savedState = getArguments();
        ArrayList<Asset> assets = savedState != null ? (ArrayList<Asset>) savedState.getSerializable(ARG_ASSETS) : null;

        VodListAdapter adapter = new VodListAdapter(assets, this);
        list.setAdapter(adapter);
    }

    @Override
    public void onAssetClicked(Asset asset) {
        PlayerFragment playerFragment = PlayerFragment.newInstance(asset);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, playerFragment)
                .addToBackStack(null)
                .commit();
    }
}
