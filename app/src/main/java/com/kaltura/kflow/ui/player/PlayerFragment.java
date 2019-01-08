package com.kaltura.kflow.ui.player;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.kaltura.client.enums.SocialActionType;
import com.kaltura.client.services.FavoriteService;
import com.kaltura.client.services.SearchHistoryService;
import com.kaltura.client.services.SocialActionService;
import com.kaltura.client.types.Asset;
import com.kaltura.client.types.Favorite;
import com.kaltura.client.types.FavoriteFilter;
import com.kaltura.client.types.SocialAction;
import com.kaltura.client.types.SocialActionFilter;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.Settings;
import com.kaltura.kflow.ui.debug.DebugFragment;
import com.kaltura.kflow.ui.main.MainActivity;
import com.kaltura.kflow.utils.ApiHelper;
import com.kaltura.kflow.utils.Utils;
import com.kaltura.playkit.PKMediaConfig;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PlayKitManager;
import com.kaltura.playkit.Player;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.PlayerState;
import com.kaltura.playkit.plugins.ads.AdEvent;
import com.kaltura.playkit.plugins.ott.OttEvent;
import com.kaltura.playkit.providers.MediaEntryProvider;
import com.kaltura.playkit.providers.api.SimpleSessionProvider;
import com.kaltura.playkit.providers.api.phoenix.APIDefines;
import com.kaltura.playkit.providers.base.OnMediaLoadCompletion;
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;

/**
 * Created by alex_lytvynenko on 04.12.2018.
 */
public class PlayerFragment extends DebugFragment {

    private static final String ARG_ASSET = "extra_asset";

    private static final String Format = "DASH_Main";

    private SwitchCompat mLike;
    private SwitchCompat mFavorite;
    private PlaybackControlsView mPlayerControls;
    private Player mPlayer;
    private Asset mAsset;
    private String mLikeId = "";

    public static PlayerFragment newInstance(Asset asset) {
        PlayerFragment likeFragment = new PlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ASSET, asset);
        likeFragment.setArguments(bundle);
        return likeFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainActivity) requireActivity()).getSupportActionBar().setTitle("Player");

        Bundle savedState = getArguments();
        if (savedState != null) {
            mAsset = (Asset) savedState.getSerializable(ARG_ASSET);
        }

        mLike = getView().findViewById(R.id.like);
        mFavorite = getView().findViewById(R.id.favorite);
        mLike.setOnCheckedChangeListener((compoundButton, b) -> {
            if (mLike.isPressed()) actionLike();
        });
        mFavorite.setOnCheckedChangeListener((compoundButton, b) -> {
            if (mFavorite.isPressed()) actionFavorite();
        });

        AppCompatTextView assetTitle = getView().findViewById(R.id.asset_title);
        assetTitle.setText(mAsset.getName());

        initPlayer();
        getLikeList();
        getFavoriteList();
    }

    private void initPlayer() {
        startOttMediaLoading(response ->
                requireActivity().runOnUiThread(() -> {
                    if (response.getResponse() != null) {
                        onMediaLoaded(response.getResponse());
                    } else {
                        Toast.makeText(requireContext(), "failed to fetch media data: " + (response.getError() != null ? response.getError().getMessage() : ""), Toast.LENGTH_LONG).show();
                    }
                }));
    }

    private void startOttMediaLoading(final OnMediaLoadCompletion completion) {
        MediaEntryProvider mediaProvider = new PhoenixMediaProvider()
                .setSessionProvider(new SimpleSessionProvider(Settings.host + "/api_v3/", Settings.partnerID, ApiHelper.getClient().getKs()))
                .setAssetId(mAsset.getId().toString())
                .setProtocol(PhoenixMediaProvider.HttpProtocol.Https)
                .setContextType(APIDefines.PlaybackContextType.Playback)
                .setAssetType(APIDefines.KalturaAssetType.Media)
                .setFormats(Format);

        mediaProvider.load(completion);
    }

    private void onMediaLoaded(PKMediaEntry mediaEntry) {

        PKMediaConfig mediaConfig = new PKMediaConfig().setMediaEntry(mediaEntry).setStartPosition(0L);
        if (mPlayer == null) {

            mPlayer = PlayKitManager.loadPlayer(requireContext(), new PKPluginConfigs());

            mPlayer.getSettings().setSecureSurface(false);

            addPlayerListeners();

            FrameLayout layout = getView().findViewById(R.id.player_layout);
            layout.addView(mPlayer.getView());

            mPlayerControls = getView().findViewById(R.id.player_controls);
            mPlayerControls.setPlayer(mPlayer);
        }

        mPlayer.prepare(mediaConfig);
        mPlayer.play();
    }

    private void addPlayerListeners() {

        mPlayer.addEventListener(event -> mPlayerControls.setPlayerState(PlayerState.READY), AdEvent.Type.CONTENT_PAUSE_REQUESTED);

        mPlayer.addStateChangeListener(event -> {
            if (event instanceof PlayerEvent.StateChanged) {
                PlayerEvent.StateChanged stateChanged = (PlayerEvent.StateChanged) event;
                if (mPlayerControls != null) {
                    mPlayerControls.setPlayerState(stateChanged.newState);
                }
            }
        });

        mPlayer.addEventListener(event -> {
            //When the track data available, this event occurs. It brings the info object with it.
            PlayerEvent.Error playerError = (PlayerEvent.Error) event;
            if (playerError != null && playerError.error != null) {
                Toast.makeText(requireContext(), "PlayerEvent.Error event  position = " + playerError.error.errorType + " errorMessage = " + playerError.error.message, Toast.LENGTH_LONG).show();
            }
        }, PlayerEvent.Type.ERROR);

        //OLD WAY FOR GETTING THE CONCURRENCY
        mPlayer.addEventListener(event -> Toast.makeText(requireContext(), "Concurrency event", Toast.LENGTH_LONG).show(), OttEvent.OttEventType.Concurrency);
    }

    private void getLikeList() {
        if (Utils.hasInternetConnection(requireContext())) {
            SocialActionFilter socialActionFilter = new SocialActionFilter();
            socialActionFilter.setAssetIdIn(mAsset.getId().toString());

            RequestBuilder requestBuilder = SocialActionService.list(socialActionFilter)
                    .setCompletion(result -> {
                        if (result.isSuccess()) {
                            for (SocialAction socialAction : result.results.getObjects()) {
                                if (socialAction.getActionType() == SocialActionType.LIKE) {
                                    mLikeId = socialAction.getId();
                                    mLike.setChecked(true);
                                    break;
                                }
                            }
                        }
                    });
            ApiHelper.execute(requestBuilder);
            clearDebugView();
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void getFavoriteList() {
        if (Utils.hasInternetConnection(requireContext())) {
            FavoriteFilter favoriteFilter = new FavoriteFilter();
            favoriteFilter.setMediaIdIn(mAsset.getId().toString());

            RequestBuilder requestBuilder = FavoriteService.list(favoriteFilter)
                    .setCompletion(result -> {
                        if (result.isSuccess()) {
                            if (result.results.getObjects() != null && !result.results.getObjects().isEmpty()) {
                                mFavorite.setChecked(true);
                            }
                        }
                    });
            ApiHelper.execute(requestBuilder);
            clearDebugView();
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void actionLike() {
        if (Utils.hasInternetConnection(requireContext())) {
            RequestBuilder requestBuilder;
            if (TextUtils.isEmpty(mLikeId)) {
                SocialAction socialAction = new SocialAction();
                socialAction.setAssetId(mAsset.getId());
                socialAction.setActionType(SocialActionType.LIKE);

                requestBuilder = SocialActionService.add(socialAction)
                        .setCompletion(result -> {
                            mLike.setEnabled(true);

                            if (result.isSuccess())
                                mLikeId = result.results.getSocialAction().getId();
                            else mLike.setChecked(false);
                        });
            } else {
                requestBuilder = SocialActionService.delete(mLikeId)
                        .setCompletion(result -> {
                            mLike.setEnabled(true);

                            if (result.isSuccess()) mLikeId = "";
                            else mLike.setChecked(true);
                        });
            }
            ApiHelper.execute(requestBuilder);
            clearDebugView();
            mLike.setEnabled(false);
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void actionFavorite() {
        if (Utils.hasInternetConnection(requireContext())) {
            RequestBuilder requestBuilder;
            if (mFavorite.isChecked()) {
                mFavorite.setEnabled(false);

                Favorite favorite = new Favorite();
                favorite.setAssetId(mAsset.getId());

                requestBuilder = FavoriteService.add(favorite)
                        .setCompletion(result -> {
                            mFavorite.setEnabled(true);
                            if (!result.isSuccess()) {
                                mFavorite.setChecked(false);
                            }
                        });
            } else {
                requestBuilder = FavoriteService.delete(mAsset.getId().intValue())
                        .setCompletion(result -> {
                            mFavorite.setEnabled(true);
                            if (!result.isSuccess()) {
                                mFavorite.setChecked(true);
                            }
                        });
            }
            ApiHelper.execute(requestBuilder);
            clearDebugView();
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    public void onPause() {
        super.onPause();
        if (mPlayerControls != null) {
            mPlayerControls.release();
        }
        if (mPlayer != null) {
            mPlayer.onApplicationPaused();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlayer != null) {
            mPlayer.onApplicationResumed();
        }
        if (mPlayerControls != null) {
            mPlayerControls.resume();
        }
    }
}
