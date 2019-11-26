package com.kaltura.kflow.presentation.assetList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.kaltura.client.types.Asset;
import com.kaltura.client.types.ProgramAsset;
import com.kaltura.kflow.R;
import com.kaltura.kflow.presentation.player.PlayerFragment;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.utils.Utils;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.providers.api.phoenix.APIDefines;

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
public class AssetListFragment extends Fragment implements AssetListAdapter.OnAssetClickListener {

    private static final String ARG_ASSETS = "extra_assets";
    private static final String ARG_SCROLL_TO_LIVE = "extra_scroll_to_live";

    public static AssetListFragment newInstance(ArrayList<Asset> assets) {
        AssetListFragment assetListFragment = new AssetListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ASSETS, assets);
        assetListFragment.setArguments(bundle);
        return assetListFragment;
    }

    public static AssetListFragment newInstance(ArrayList<Asset> assets, boolean scrollToLive) {
        AssetListFragment assetListFragment = new AssetListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ASSETS, assets);
        bundle.putBoolean(ARG_SCROLL_TO_LIVE, scrollToLive);
        assetListFragment.setArguments(bundle);
        return assetListFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vod_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Asset list");

        initList();
    }

    private void initList() {
        RecyclerView list = getView().findViewById(R.id.list);
        list.setHasFixedSize(true);
        list.setLayoutManager(new LinearLayoutManager(requireContext()));
        list.addItemDecoration(new DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL));

        Bundle savedState = getArguments();
        ArrayList<Asset> assets = savedState != null ? (ArrayList<Asset>) savedState.getSerializable(ARG_ASSETS) : null;
        boolean scrollToLive = savedState != null && savedState.getBoolean(ARG_SCROLL_TO_LIVE);

        AssetListAdapter adapter = new AssetListAdapter(assets, this);
        list.setAdapter(adapter);

        if (scrollToLive && assets != null) {
            int liveAssetPosition = 0;
            for (Asset asset : assets) {
                if (asset instanceof ProgramAsset && Utils.isProgramInLive(asset)) {
                    liveAssetPosition = assets.indexOf(asset);
                    break;
                }
            }

            if (liveAssetPosition > 2)
                liveAssetPosition -= 3; // minus 3 items from the top, to move live asset to the middle of the screen

            list.scrollToPosition(liveAssetPosition);
        }
    }

    @Override
    public void onVodAssetClicked(Asset asset) {
        PlayerFragment playerFragment = PlayerFragment.newInstance(asset, false);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, playerFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onProgramAssetClicked(Asset asset, APIDefines.PlaybackContextType contextType) {
        if (!Utils.isProgramInFuture(asset)) {
            PlayerFragment playerFragment = PlayerFragment.newInstance(asset, false, contextType);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, playerFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
