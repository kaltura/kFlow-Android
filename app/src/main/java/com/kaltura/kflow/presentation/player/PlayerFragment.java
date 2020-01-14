package com.kaltura.kflow.presentation.player;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.kaltura.client.enums.AssetReferenceType;
import com.kaltura.client.enums.AssetType;
import com.kaltura.client.enums.PinType;
import com.kaltura.client.enums.RuleType;
import com.kaltura.client.enums.SocialActionType;
import com.kaltura.client.services.AssetService;
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
import com.kaltura.client.types.Recording;
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
import com.kaltura.playkit.PKMediaFormat;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PlayKitManager;
import com.kaltura.playkit.Player;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.PlayerState;
import com.kaltura.playkit.player.LoadControlBuffers;
import com.kaltura.playkit.player.PKTracks;
import com.kaltura.playkit.player.TextTrack;
import com.kaltura.playkit.plugins.SamplePlugin;
import com.kaltura.playkit.plugins.ads.AdEvent;
import com.kaltura.playkit.plugins.kava.KavaAnalyticsPlugin;
import com.kaltura.playkit.plugins.ott.OttEvent;
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsConfig;
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsPlugin;
import com.kaltura.playkit.plugins.youbora.YouboraPlugin;
import com.kaltura.playkit.providers.MediaEntryProvider;
import com.kaltura.playkit.providers.api.SimpleSessionProvider;
import com.kaltura.playkit.providers.api.phoenix.APIDefines;
import com.kaltura.playkit.providers.base.OnMediaLoadCompletion;
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.SwitchCompat;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex_lytvynenko on 04.12.2018.
 */
public class PlayerFragment extends DebugFragment {

    private static final String ARG_ASSET = "extra_asset";
    private static final String ARG_KEEP_ALIVE = "extra_keep_alive";
    private static final String ARG_RECORDING = "extra_recording";
    private static final String ARG_PLAYBACK_CONTEXT_TYPE = "extra_playback_context_type";
    private final static String TAG = PlayerFragment.class.getCanonicalName();

    private SwitchCompat mLike;
    private SwitchCompat mFavorite;
    private AppCompatButton mCheckAll;
    private AppCompatButton mInsertPin;
    private AppCompatSpinner mSubtitles;
    private LinearLayout mPinLayout;
    private TextInputLayout mPinInputLayout;
    private TextInputEditText mPin;
    private PlaybackControlsView mPlayerControls;
    private Player mPlayer;
    private Asset mAsset;
    private Recording mRecording;
    private String mLikeId = "";
    private PKMediaEntry mediaEntry;
    private int mParentalRuleId;
    private boolean mIsKeepAlive;
    private PlayerKeepAliveService playerKeepAliveService;
    private APIDefines.PlaybackContextType initialPlaybackContextType;

    public static PlayerFragment newInstance(Asset asset, boolean isKeepAlive) {
        PlayerFragment likeFragment = new PlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ASSET, asset);
        bundle.putBoolean(ARG_KEEP_ALIVE, isKeepAlive);
        likeFragment.setArguments(bundle);
        return likeFragment;
    }

    public static PlayerFragment newInstance(Asset asset, boolean isKeepAlive, APIDefines.PlaybackContextType playbackContextType) {
        PlayerFragment likeFragment = new PlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_ASSET, asset);
        bundle.putBoolean(ARG_KEEP_ALIVE, isKeepAlive);
        bundle.putSerializable(ARG_PLAYBACK_CONTEXT_TYPE, playbackContextType);
        likeFragment.setArguments(bundle);
        return likeFragment;
    }

    public static PlayerFragment newInstance(Recording recording) {
        PlayerFragment likeFragment = new PlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_RECORDING, recording);
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
        initUI();

        Bundle savedState = getArguments();
        if (savedState != null) {
            mAsset = (Asset) savedState.getSerializable(ARG_ASSET);
            mIsKeepAlive = savedState.getBoolean(ARG_KEEP_ALIVE);
            mRecording = (Recording) savedState.getSerializable(ARG_RECORDING);
            initialPlaybackContextType = (APIDefines.PlaybackContextType) savedState.getSerializable(ARG_PLAYBACK_CONTEXT_TYPE);
        }

        playerKeepAliveService = new PlayerKeepAliveService();

        if (mAsset == null && mRecording != null) loadAsset(mRecording.getAssetId());
        else onAssetLoaded();
    }

    private void initUI() {
        mLike = getView().findViewById(R.id.like);
        mFavorite = getView().findViewById(R.id.favorite);
        mPlayerControls = getView().findViewById(R.id.player_controls);
        mCheckAll = getView().findViewById(R.id.check_all);
        mInsertPin = getView().findViewById(R.id.insert_pin);
        mPinLayout = getView().findViewById(R.id.pin_layout);
        mSubtitles = getView().findViewById(R.id.subtitles);
        mPin = getView().findViewById(R.id.pin);
        mPinInputLayout = getView().findViewById(R.id.pin_input_layout);

        mLike.setOnCheckedChangeListener((compoundButton, b) -> {
            if (mLike.isPressed()) actionLike();
        });
        mFavorite.setOnCheckedChangeListener((compoundButton, b) -> {
            if (mFavorite.isPressed()) actionFavorite();
        });
        mPlayerControls.setOnStartOverClickListener(view1 -> {
            if (mediaEntry.getMediaType().equals(PKMediaEntry.MediaEntryType.Vod))
                mPlayer.replay();
            else if (mAsset instanceof ProgramAsset && Utils.isProgramInPast(mAsset))
                initPlayer(APIDefines.PlaybackContextType.Catchup);
            else if (mAsset instanceof ProgramAsset && Utils.isProgramInLive(mAsset))
                initPlayer(APIDefines.PlaybackContextType.StartOver);
        });

        mCheckAll.setOnClickListener(view1 ->

        {
            Utils.hideKeyboard(getView());
            checkAllTogetherRequest();
        });
        mInsertPin.setOnClickListener(view1 ->

        {
            Utils.hideKeyboard(getView());
            if (mPinInputLayout.getVisibility() == View.GONE) {
                showPinInput();
            } else {
                checkPinRequest(mPin.getText().toString());
            }
        });
    }

    private void loadAsset(long assetId) {
        if (Utils.hasInternetConnection(requireContext())) {
            RequestBuilder requestBuilder = AssetService.get(String.valueOf(assetId), AssetReferenceType.EPG_INTERNAL)
                    .setCompletion(result -> {
                        if (result.isSuccess()) {
                            mAsset = result.results;
                            onAssetLoaded();
                        }
                    });
            PhoenixApiManager.execute(requestBuilder);
            clearDebugView();
        } else {
            Toast.makeText(requireContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void onAssetLoaded() {
        ((AppCompatTextView) getView().findViewById(R.id.asset_title)).setText(mAsset.getName());

        registerPlugins();
        initPlayer(getPlaybackContextType());
        getLikeList();
        getFavoriteList();
    }

    private void initPlayer(APIDefines.PlaybackContextType playbackContextType) {
        startOttMediaLoading(playbackContextType, response -> {
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    if (response.getResponse() != null) {
                        mediaEntry = response.getResponse();
                        if (mIsKeepAlive) {
                            onMediaLoadedKeepAlive();
                        } else {
                            onMediaLoaded();
                        }
                    } else {
                        Toast.makeText(requireContext(), "failed to fetch media data: " + (response.getError() != null ? response.getError().getMessage() : ""), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void registerPlugins() {
        PlayKitManager.registerPlugins(requireContext(), SamplePlugin.factory);
//		PlayKitManager.registerPlugins(requireContext(), KalturaStatsPlugin.factory);
        PlayKitManager.registerPlugins(requireContext(), KavaAnalyticsPlugin.factory);
        PlayKitManager.registerPlugins(requireContext(), YouboraPlugin.factory);
        PlayKitManager.registerPlugins(requireContext(), PhoenixAnalyticsPlugin.factory);
    }

    private void configurePlugins(PKPluginConfigs pluginConfigs) {

        addPhoenixAnalyticsPluginConfig(pluginConfigs);

    }

    private void addPhoenixAnalyticsPluginConfig(PKPluginConfigs config) {
        String ks = PhoenixApiManager.getClient().getKs();
        int pId = PreferenceManager.getInstance(requireContext()).getPartnerId();
        String baseUrl = PreferenceManager.getInstance(requireContext()).getBaseUrl() + "/api_v3/";
        PhoenixAnalyticsConfig phoenixAnalyticsConfig = new PhoenixAnalyticsConfig(pId, baseUrl, ks, 30);
        config.setPluginConfig(PhoenixAnalyticsPlugin.factory.getName(), phoenixAnalyticsConfig);
    }

    private void startOttMediaLoading(APIDefines.PlaybackContextType playbackContextType, final OnMediaLoadCompletion completion) {
        MediaEntryProvider mediaProvider = new PhoenixMediaProvider()
                .setSessionProvider(new SimpleSessionProvider(PreferenceManager.getInstance(requireContext()).getBaseUrl() + "/api_v3/", PreferenceManager.getInstance(requireContext()).getPartnerId(), PhoenixApiManager.getClient().getKs()))
                .setAssetId(getAssetIdByFlowType())
                .setProtocol(PhoenixMediaProvider.HttpProtocol.All)
                .setContextType(playbackContextType)
                .setAssetReferenceType(getAssetReferenceType(playbackContextType))
                .setAssetType(getAssetType(playbackContextType))
                .setFormats(PreferenceManager.getInstance(requireContext()).getMediaFileFormat());

        mediaProvider.load(completion);
    }

    private String getAssetIdByFlowType() {
        if (mAsset instanceof ProgramAsset && getPlaybackContextType() == APIDefines.PlaybackContextType.Playback)
            return String.valueOf(((ProgramAsset) mAsset).getLinearAssetId());
        else if (mRecording == null) return String.valueOf(mAsset.getId());
        else return String.valueOf(mRecording.getId());
    }

    private APIDefines.KalturaAssetType getAssetType(APIDefines.PlaybackContextType playbackContextType) {
        if (playbackContextType == APIDefines.PlaybackContextType.StartOver
                || playbackContextType == APIDefines.PlaybackContextType.Catchup)
            return APIDefines.KalturaAssetType.Epg;
        else if (mRecording != null) return APIDefines.KalturaAssetType.Recording;
        else return APIDefines.KalturaAssetType.Media;
    }

    private APIDefines.PlaybackContextType getPlaybackContextType() {
        if (initialPlaybackContextType != null) return initialPlaybackContextType;

        if (mAsset instanceof ProgramAsset && Utils.isProgramInPast(mAsset))
            return APIDefines.PlaybackContextType.Catchup;
        else if (mAsset instanceof ProgramAsset && Utils.isProgramInLive(mAsset))
            return APIDefines.PlaybackContextType.Playback;
        else
            return APIDefines.PlaybackContextType.Playback;
    }

    private APIDefines.AssetReferenceType getAssetReferenceType(APIDefines.PlaybackContextType playbackContextType) {
        if (playbackContextType == APIDefines.PlaybackContextType.StartOver
                || playbackContextType == APIDefines.PlaybackContextType.Catchup)
            return APIDefines.AssetReferenceType.InternalEpg;
        else if (mRecording == null) return APIDefines.AssetReferenceType.Media;
        else return APIDefines.AssetReferenceType.Media;
    }

    private void loadPlayerSettings() {
        if (mPlayer == null) {
            PKPluginConfigs pluginConfig = new PKPluginConfigs();
            configurePlugins(pluginConfig);
            mPlayer = PlayKitManager.loadPlayer(requireContext(), pluginConfig);

            mPlayer.getSettings().setSecureSurface(false);
            mPlayer.getSettings().setAllowCrossProtocolRedirect(true);
            mPlayer.getSettings().setCea608CaptionsEnabled(true); // default is false

            addPlayerListeners();

            FrameLayout layout = getView().findViewById(R.id.player_layout);
            layout.addView(mPlayer.getView());

            mPlayerControls.setPlayer(mPlayer);
        }
    }

    private void onMediaLoadedKeepAlive() {

        loadPlayerSettings();

        String sourceUrl = "";
        List<PKMediaSource> sources = mediaEntry.getSources();
        for (PKMediaSource pkms : sources) {
            if (pkms.getMediaFormat().equals(PKMediaFormat.dash)) {
                sourceUrl = pkms.getUrl();
            }
        }

        try {
            getKeepAliveHeaderUrl(new URL(sourceUrl), (status, url) -> {
                if (mediaEntry.getSources() != null && !mediaEntry.getSources().isEmpty()) {
                    Log.d(TAG, "The KeepAlive Url is : " + url);
                    playerKeepAliveService.setKeepAliveURL(url);
                    mediaEntry.getSources().get(0).setUrl(url);
                }

                setMediaEntry();
            });
        } catch (MalformedURLException e) {
            Log.d(TAG, "The KeepAlive Url is : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onMediaLoaded() {
        loadPlayerSettings();
        setMediaEntry();
    }

    private void setMediaEntry() {

        PKMediaConfig mediaConfig = new PKMediaConfig().setMediaEntry(mediaEntry);
        if (mediaEntry.getMediaType().equals(PKMediaEntry.MediaEntryType.Live)) {
            mediaEntry.setMediaType(PKMediaEntry.MediaEntryType.DvrLive);
            mPlayerControls.setAsset(mAsset);
//            mPlayerControls.disableControllersForLive();
        } else {
            mediaConfig.setStartPosition(0L);
        }

        mPlayer.prepare(mediaConfig);
        mPlayer.play();

    }

    private void getKeepAliveHeaderUrl(final URL url, final KeepAliveUrlResultListener listener) {
        new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(false);
                final String keepAliveURL = conn.getHeaderField("Location");
                final boolean isSuccess = !TextUtils.isEmpty(keepAliveURL) && conn.getResponseCode() == 307;
                if (isSuccess) {
                    requireActivity().runOnUiThread(() -> {
                        listener.onResult(true, keepAliveURL);
                    });
                } else {
                    URL url1 = new URL(keepAliveURL);
                    getKeepAliveHeaderUrl(url1, listener);
                }

            } catch (Exception e) {
                listener.onResult(false, "Failed to retreive Location header : " + e.getMessage());
                e.printStackTrace();
            }

        }).start();

    }

    private void addPlayerListeners() {

        mPlayer.addListener(this, PlayerEvent.tracksAvailable, event -> {
            //When the track data available, this event occurs. It brings the info object with it.
            if (event != null && event.tracksInfo != null && !event.tracksInfo.getTextTracks().isEmpty()) {
                TextTrack defaultTextTrack = getDefaultTextTrack(event.tracksInfo);
                initSubtitles(event.tracksInfo.getTextTracks(), defaultTextTrack);
                changeTextTrack(defaultTextTrack);
            }
        });

        mPlayer.addListener(this, AdEvent.Type.CONTENT_PAUSE_REQUESTED, event ->
                mPlayerControls.setPlayerState(PlayerState.READY)
        );

        mPlayer.addListener(this, PlayerEvent.pause, event -> {

        });

        mPlayer.addListener(this, PlayerEvent.play, event -> {
            if (mIsKeepAlive) playerKeepAliveService.startFireKeepAliveService();
        });

        mPlayer.addListener(this, PlayerEvent.stateChanged, event -> {
            if (mPlayerControls != null) {
                mPlayerControls.setPlayerState(event.newState);
            }
        });

        mPlayer.addListener(this, PlayerEvent.Type.ERROR, event -> {
            //When the track data available, this event occurs. It brings the info object with it.
            PlayerEvent.Error playerError = (PlayerEvent.Error) event;
            if (playerError != null && playerError.error != null) {
                Toast.makeText(requireContext(), "PlayerEvent.Error event  position = " + playerError.error.errorType + " errorMessage = " + playerError.error.message, Toast.LENGTH_LONG).show();
            }
        });

        //OLD WAY FOR GETTING THE CONCURRENCY
        mPlayer.addListener(this, OttEvent.OttEventType.Concurrency, event ->
                Toast.makeText(requireContext(), "Concurrency event", Toast.LENGTH_LONG).show()
        );
    }

    private void initSubtitles(List<TextTrack> tracks, TextTrack selected) {
        List<String> languages = new ArrayList<>();
        for (TextTrack textTrack : tracks) {
            if (textTrack != null && textTrack.getLanguage() != null)
                languages.add(textTrack.getLanguage());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, languages);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSubtitles.setAdapter(adapter);
        mSubtitles.setSelection(tracks.indexOf(selected));
        mSubtitles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                changeTextTrack(tracks.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void changeTextTrack(TextTrack textTrack) {
        mPlayer.changeTrack(textTrack.getUniqueId());
    }

    private TextTrack getDefaultTextTrack(PKTracks tracksInfo) {
        TextTrack track = tracksInfo.getTextTracks().get(0);
        for (TextTrack tr : tracksInfo.getTextTracks()) {
            if (tr != null && tr.getLanguage() != null && tr.getLanguage().equalsIgnoreCase("en"))
                track = tr;
        }
        return track;
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
        if (mIsKeepAlive) {
            playerKeepAliveService.cancelFireKeepAliveService();
            mIsKeepAlive = false;
        }
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

    public interface KeepAliveUrlResultListener {
        void onResult(boolean status, String url);
    }
}
