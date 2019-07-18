package com.kaltura.kflow.presentation.player;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kaltura.client.enums.AssetType;
import com.kaltura.client.enums.PinType;
import com.kaltura.client.enums.RuleType;
import com.kaltura.client.enums.SocialActionType;
import com.kaltura.client.services.BookmarkService;
import com.kaltura.client.services.FavoriteService;
import com.kaltura.client.services.PinService;
import com.kaltura.client.services.ProductPriceService;
import com.kaltura.client.services.SocialActionService;
import com.kaltura.client.services.UserAssetRuleService;
import com.kaltura.client.types.Asset;
import com.kaltura.client.types.BookmarkFilter;
import com.kaltura.client.types.Favorite;
import com.kaltura.client.types.FavoriteFilter;
import com.kaltura.client.types.ListResponse;
import com.kaltura.client.types.ProductPriceFilter;
import com.kaltura.client.types.ProgramAsset;
import com.kaltura.client.types.SocialAction;
import com.kaltura.client.types.SocialActionFilter;
import com.kaltura.client.types.UserAssetRule;
import com.kaltura.client.types.UserAssetRuleFilter;
import com.kaltura.client.utils.request.MultiRequestBuilder;
import com.kaltura.client.utils.request.RequestBuilder;
import com.kaltura.kflow.R;
import com.kaltura.kflow.manager.PreferenceManager;
import com.kaltura.kflow.presentation.debug.DebugFragment;
import com.kaltura.kflow.presentation.main.MainActivity;
import com.kaltura.kflow.manager.PhoenixApiManager;
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
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;

import java.util.List;

/**
 * Created by alex_lytvynenko on 04.12.2018.
 */
public class PlayerFragment extends DebugFragment {

    private static final String ARG_ASSET = "extra_asset";

    private SwitchCompat mLike;
    private SwitchCompat mFavorite;
    private AppCompatButton mCheckAll;
    private AppCompatButton mInsertPin;
    private LinearLayout mPinLayout;
    private TextInputLayout mPinInputLayout;
    private TextInputEditText mPin;
    private PlaybackControlsView mPlayerControls;
    private Player mPlayer;
    private Asset mAsset;
    private String mLikeId = "";
    private PKMediaEntry mediaEntry;
    private int mParentalRuleId;

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
        mPlayerControls = getView().findViewById(R.id.player_controls);
        mCheckAll = getView().findViewById(R.id.check_all);
        mInsertPin = getView().findViewById(R.id.insert_pin);
        mPinLayout = getView().findViewById(R.id.pin_layout);
        mPin = getView().findViewById(R.id.pin);
        mPinInputLayout = getView().findViewById(R.id.pin_input_layout);

        mLike.setOnCheckedChangeListener((compoundButton, b) -> {
            if (mLike.isPressed()) actionLike();
        });
        mFavorite.setOnCheckedChangeListener((compoundButton, b) -> {
            if (mFavorite.isPressed()) actionFavorite();
        });
        mPlayerControls.setOnStartOverClickListener(view1 -> {
            if (mediaEntry.getMediaType().equals(PKMediaEntry.MediaEntryType.Vod)) {
                mPlayer.replay();
            }
        });

        mCheckAll.setOnClickListener(view1 -> {
            Utils.hideKeyboard(getView());
            checkAllTogetherRequest();
        });
        mInsertPin.setOnClickListener(view1 -> {
            Utils.hideKeyboard(getView());
            if (mPinInputLayout.getVisibility() == View.GONE) {
                showPinInput();
            } else {
                checkPinRequest(mPin.getText().toString());
            }
        });

        AppCompatTextView assetTitle = getView().findViewById(R.id.asset_title);
        assetTitle.setText(mAsset.getName());

        boolean isInPast = false;

        if (mAsset instanceof ProgramAsset) isInPast = Utils.isProgramIsPast(mAsset);

        initPlayer(isInPast ? APIDefines.PlaybackContextType.Catchup : APIDefines.PlaybackContextType.Playback);
        getLikeList();
        getFavoriteList();
    }

    private void initPlayer(APIDefines.PlaybackContextType contextType) {
        startOttMediaLoading(contextType, response -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (response.getResponse() != null) {
                        mediaEntry = response.getResponse();
                        onMediaLoaded();
                    } else {
                        Toast.makeText(requireContext(), "failed to fetch media data: " + (response.getError() != null ? response.getError().getMessage() : ""), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void startOttMediaLoading(APIDefines.PlaybackContextType contextType, final OnMediaLoadCompletion completion) {
        MediaEntryProvider mediaProvider = new PhoenixMediaProvider()
                .setSessionProvider(new SimpleSessionProvider(PreferenceManager.getInstance(requireContext()).getBaseUrl() + "/api_v3/", PreferenceManager.getInstance(requireContext()).getPartnerId(), PhoenixApiManager.getClient().getKs()))
                .setAssetId(String.valueOf(mAsset.getId()))
                .setProtocol(PhoenixMediaProvider.HttpProtocol.Https)
                .setContextType(contextType)
                .setAssetReferenceType(contextType == APIDefines.PlaybackContextType.Playback ? APIDefines.AssetReferenceType.Media : APIDefines.AssetReferenceType.InternalEpg)
                .setAssetType(contextType == APIDefines.PlaybackContextType.Playback ? APIDefines.KalturaAssetType.Media : APIDefines.KalturaAssetType.Epg)
                .setFormats(PreferenceManager.getInstance(requireContext()).getMediaFileFormat());

        mediaProvider.load(completion);
    }

    private void onMediaLoaded() {

        PKMediaConfig mediaConfig = new PKMediaConfig().setMediaEntry(mediaEntry);
        if (mediaEntry.getMediaType().equals(PKMediaEntry.MediaEntryType.Live)) {
            mPlayerControls.setAsset(mAsset);
            mPlayerControls.disableControllersForLive();
        } else {
            mediaConfig.setStartPosition(0L);
        }

        if (mPlayer == null) {

            mPlayer = PlayKitManager.loadPlayer(requireContext(), new PKPluginConfigs());

            mPlayer.getSettings().setSecureSurface(false);
            mPlayer.getSettings().setAllowCrossProtocolRedirect(true);

            addPlayerListeners();

            FrameLayout layout = getView().findViewById(R.id.player_layout);
            layout.addView(mPlayer.getView());

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
            PhoenixApiManager.execute(requestBuilder);
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
            PhoenixApiManager.execute(requestBuilder);
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
            PhoenixApiManager.execute(requestBuilder);
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
            PhoenixApiManager.execute(requestBuilder);
            clearDebugView();
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAllTogetherRequest() {
        if (Utils.hasInternetConnection(requireContext())) {

            if (TextUtils.isDigitsOnly(mAsset.getId().toString())) {
                MultiRequestBuilder multiRequestBuilder = new MultiRequestBuilder();

                // product price request
                ProductPriceFilter productPriceFilter = new ProductPriceFilter();
                productPriceFilter.setFileIdIn(mAsset.getId().toString());
                multiRequestBuilder.add(ProductPriceService.list(productPriceFilter));

                // bookmark request
                BookmarkFilter bookmarkFilter = new BookmarkFilter();
                bookmarkFilter.setAssetIdIn(mAsset.getId().toString());
                bookmarkFilter.setAssetTypeEqual(AssetType.MEDIA);
                multiRequestBuilder.add(BookmarkService.list(bookmarkFilter));

                // asset rules request
                UserAssetRuleFilter userAssetRuleFilter = new UserAssetRuleFilter();
                userAssetRuleFilter.setAssetTypeEqual(1);
                userAssetRuleFilter.setAssetIdEqual(mAsset.getId());
                multiRequestBuilder.add(UserAssetRuleService.list(userAssetRuleFilter));

                multiRequestBuilder.setCompletion(result -> {
                    if (result.isSuccess()) {
                        if (result.results != null && result.results.get(2) != null) {
                            List<UserAssetRule> userAssetRules = ((ListResponse) result.results.get(2)).getObjects();
                            for (UserAssetRule userAssetRule : userAssetRules) {
                                if (userAssetRule.getRuleType() == RuleType.PARENTAL) {
                                    mParentalRuleId = userAssetRule.getId().intValue();
                                    mPinLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                });

                PhoenixApiManager.execute(multiRequestBuilder);
                clearDebugView();
            } else {
                Toast.makeText(requireContext(), "Wrong input", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkPinRequest(String pin) {
        if (Utils.hasInternetConnection(requireContext())) {

            if (TextUtils.isDigitsOnly(pin)) {
                RequestBuilder requestBuilder = PinService.validate(pin, PinType.PARENTAL, mParentalRuleId);
                PhoenixApiManager.execute(requestBuilder);
                clearDebugView();
            } else {
                Toast.makeText(requireContext(), "Wrong input", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPinInput() {
        mPinInputLayout.setVisibility(View.VISIBLE);
        mInsertPin.setText("Check pin");
        Utils.showKeyboard(mPin);
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
