package com.kaltura.kflow.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.kaltura.client.services.FavoriteService;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.presentation.debug.DebugFragment;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.manager.PhoenixApiManager;
import com.kaltura.kflow.utils.Utils;

import java.text.NumberFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;

/**
 * Created by alex_lytvynenko on 12/13/18.
 */
public class FavoritesFragment extends DebugFragment implements View.OnClickListener {

    private AppCompatTextView mFavoriteCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Favorites");

        mFavoriteCount = getView().findViewById(R.id.favorite_count);
        getView().findViewById(R.id.get_favorites).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Utils.hideKeyboard(getView());
        getFavoritesRequest();
    }

    private void getFavoritesRequest() {
        if (Utils.hasInternetConnection(requireContext())) {
            RequestBuilder requestBuilder = FavoriteService.list()
                    .setCompletion(result -> {
                        if (result.isSuccess()) {
                            mFavoriteCount.setText(getResources().getQuantityString(R.plurals.favorite_count,
                                    result.results.getTotalCount(), NumberFormat.getInstance().format(result.results.getTotalCount())));
                            mFavoriteCount.setVisibility(View.VISIBLE);
                        }
                    });
            mFavoriteCount.setVisibility(View.GONE);
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
