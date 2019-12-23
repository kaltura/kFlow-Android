package com.kaltura.kflow.presentation.player

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.isGone
import com.kaltura.client.enums.*
import com.kaltura.client.services.*
import com.kaltura.client.types.*
import com.kaltura.client.utils.request.MultiRequestBuilder
import com.kaltura.kflow.R
import com.kaltura.kflow.manager.PhoenixApiManager
import com.kaltura.kflow.manager.PreferenceManager
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.string
import com.kaltura.kflow.presentation.extension.visible
import com.kaltura.kflow.presentation.extension.withInternetConnection
import com.kaltura.kflow.presentation.main.MainActivity
import com.kaltura.kflow.utils.Utils
import com.kaltura.playkit.*
import com.kaltura.playkit.PlayerEvent.StateChanged
import com.kaltura.playkit.PlayerEvent.TracksAvailable
import com.kaltura.playkit.player.PKTracks
import com.kaltura.playkit.player.TextTrack
import com.kaltura.playkit.plugins.SamplePlugin
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.kava.KavaAnalyticsPlugin
import com.kaltura.playkit.plugins.ott.OttEvent
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsConfig
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsPlugin
import com.kaltura.playkit.plugins.youbora.YouboraPlugin
import com.kaltura.playkit.providers.MediaEntryProvider
import com.kaltura.playkit.providers.api.SimpleSessionProvider
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.api.phoenix.APIDefines.KalturaAssetType
import com.kaltura.playkit.providers.base.OnMediaLoadCompletion
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import kotlinx.android.synthetic.main.fragment_player.*
import org.jetbrains.anko.support.v4.toast
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by alex_lytvynenko on 04.12.2018.
 */
class PlayerFragment : DebugFragment(R.layout.fragment_player) {

    companion object {
        private val TAG = PlayerFragment::class.java.canonicalName
        const val ARG_ASSET = "extra_asset"
        const val ARG_KEEP_ALIVE = "extra_keep_alive"
        const val ARG_RECORDING = "extra_recording"
        const val ARG_PLAYBACK_CONTEXT_TYPE = "extra_playback_context_type"
    }

    private var player: Player? = null
    private var asset: Asset? = null
    private var recording: Recording? = null
    private var likeId = ""
    private lateinit var mediaEntry: PKMediaEntry
    private var parentalRuleId = 0
    private var isKeepAlive = false
    private var playerKeepAliveService = PlayerKeepAliveService()
    private var initialPlaybackContextType: APIDefines.PlaybackContextType? = null

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as MainActivity).supportActionBar?.title = "Player"

        initUI()
        if (arguments != null) {
            asset = arguments!!.getSerializable(ARG_ASSET) as Asset
            isKeepAlive = arguments!!.getBoolean(ARG_KEEP_ALIVE)
            recording = arguments!!.getSerializable(ARG_RECORDING) as Recording
            initialPlaybackContextType = arguments!!.getSerializable(ARG_PLAYBACK_CONTEXT_TYPE) as APIDefines.PlaybackContextType
        }
        if (asset == null && recording != null) loadAsset(recording!!.assetId) else onAssetLoaded()
    }

    private fun initUI() {
        like.setOnCheckedChangeListener { _, _ ->
            if (like.isPressed) actionLike()
        }
        favorite.setOnCheckedChangeListener { _, _ ->
            if (favorite.isPressed) actionFavorite()
        }
        playerControls.setOnStartOverClickListener(View.OnClickListener {
            if (mediaEntry.mediaType == PKMediaEntry.MediaEntryType.Vod) player?.replay()
            else if (asset is ProgramAsset && Utils.isProgramInPast(asset)) initPlayer(APIDefines.PlaybackContextType.Catchup)
            else if (asset is ProgramAsset && Utils.isProgramInLive(asset)) initPlayer(APIDefines.PlaybackContextType.StartOver)
        })
        checkAll.setOnClickListener {
            Utils.hideKeyboard(view)
            checkAllTogetherRequest()
        }
        insertPin.setOnClickListener {
            Utils.hideKeyboard(view)
            if (pinInputLayout.isGone) showPinInput()
            else checkPinRequest(pin.string)
        }
    }

    private fun loadAsset(assetId: Long) {
        withInternetConnection {
            PhoenixApiManager.execute(AssetService.get(assetId.toString(), AssetReferenceType.EPG_INTERNAL).setCompletion {
                if (it.isSuccess) {
                    asset = it.results
                    onAssetLoaded()
                }
            })
            clearDebugView()
        }
    }

    private fun onAssetLoaded() {
        assetTitle.text = asset?.name ?: ""
        registerPlugins()
        initPlayer(getPlaybackContextType())
        likeList()
        favoriteList()
    }

    private fun initPlayer(playbackContextType: APIDefines.PlaybackContextType) {
        startOttMediaLoading(playbackContextType, OnMediaLoadCompletion {
            if (isAdded) {
                requireActivity().runOnUiThread {
                    if (it.response != null) {
                        mediaEntry = it.response
                        if (isKeepAlive) onMediaLoadedKeepAlive()
                        else onMediaLoaded()
                    } else {
                        toast("failed to fetch media data: ${it.error?.message ?: ""}")
                    }
                }
            }
        })
    }

    private fun registerPlugins() {
        PlayKitManager.registerPlugins(requireContext(), SamplePlugin.factory)
        //		PlayKitManager.registerPlugins(requireContext(), KalturaStatsPlugin.factory);
        PlayKitManager.registerPlugins(requireContext(), KavaAnalyticsPlugin.factory)
        PlayKitManager.registerPlugins(requireContext(), YouboraPlugin.factory)
        PlayKitManager.registerPlugins(requireContext(), PhoenixAnalyticsPlugin.factory)
    }

    private fun configurePlugins(pluginConfigs: PKPluginConfigs) {
        addPhoenixAnalyticsPluginConfig(pluginConfigs)
    }

    private fun addPhoenixAnalyticsPluginConfig(config: PKPluginConfigs) {
        val ks = PhoenixApiManager.getClient().ks
        val pId = PreferenceManager.with(requireContext()).partnerId
        val baseUrl = PreferenceManager.with(requireContext()).baseUrl + "/api_v3/"
        val phoenixAnalyticsConfig = PhoenixAnalyticsConfig(pId, baseUrl, ks, 30)
        config.setPluginConfig(PhoenixAnalyticsPlugin.factory.name, phoenixAnalyticsConfig)
    }

    private fun startOttMediaLoading(playbackContextType: APIDefines.PlaybackContextType, completion: OnMediaLoadCompletion) {
        val mediaProvider: MediaEntryProvider = PhoenixMediaProvider()
                .setSessionProvider(SimpleSessionProvider(PreferenceManager.with(requireContext()).baseUrl + "/api_v3/", PreferenceManager.with(requireContext()).partnerId, PhoenixApiManager.getClient().ks))
                .setAssetId(getAssetIdByFlowType())
                .setProtocol(PhoenixMediaProvider.HttpProtocol.All)
                .setContextType(playbackContextType)
                .setAssetReferenceType(getAssetReferenceType(playbackContextType))
                .setAssetType(getAssetType(playbackContextType))
                .setFormats(PreferenceManager.with(requireContext()).mediaFileFormat)
        mediaProvider.load(completion)
    }

    private fun getAssetIdByFlowType(): String = when {
        (asset is ProgramAsset && getPlaybackContextType() == APIDefines.PlaybackContextType.Playback) -> (asset as ProgramAsset).linearAssetId.toString()
        recording == null -> asset!!.id.toString()
        else -> recording!!.id.toString()
    }

    private fun getAssetType(playbackContextType: APIDefines.PlaybackContextType): KalturaAssetType = when {
        (playbackContextType == APIDefines.PlaybackContextType.StartOver || playbackContextType == APIDefines.PlaybackContextType.Catchup) -> KalturaAssetType.Epg
        recording != null -> KalturaAssetType.Recording
        else -> KalturaAssetType.Media
    }

    private fun getPlaybackContextType(): APIDefines.PlaybackContextType = when {
        initialPlaybackContextType != null -> initialPlaybackContextType!!
        asset is ProgramAsset && Utils.isProgramInPast(asset) -> APIDefines.PlaybackContextType.Catchup
        asset is ProgramAsset && Utils.isProgramInLive(asset) -> APIDefines.PlaybackContextType.Playback
        else -> APIDefines.PlaybackContextType.Playback
    }

    private fun getAssetReferenceType(playbackContextType: APIDefines.PlaybackContextType): APIDefines.AssetReferenceType = when {
        playbackContextType == APIDefines.PlaybackContextType.StartOver || playbackContextType == APIDefines.PlaybackContextType.Catchup -> APIDefines.AssetReferenceType.InternalEpg
        recording == null -> APIDefines.AssetReferenceType.Media
        else -> APIDefines.AssetReferenceType.Npvr
    }

    private fun loadPlayerSettings() {
        if (player == null) {
            val pluginConfig = PKPluginConfigs()
            configurePlugins(pluginConfig)
            player = PlayKitManager.loadPlayer(requireContext(), pluginConfig).apply {
                settings.setSecureSurface(false)
                settings.setAllowCrossProtocolRedirect(true)
                settings.setCea608CaptionsEnabled(true) // default is false
            }

            addPlayerListeners()
            playerLayout.addView(player?.view)
            playerControls.player = player
        }
    }

    private fun onMediaLoadedKeepAlive() {
        loadPlayerSettings()
        var sourceUrl = ""
        val sources = mediaEntry.sources
        sources.forEach {
            if (it.mediaFormat == PKMediaFormat.dash) sourceUrl = it.url
        }
        try {
            getKeepAliveHeaderUrl(URL(sourceUrl)) { status, url ->
                if (mediaEntry.sources != null && mediaEntry.sources.isNotEmpty()) {
                    Log.d(TAG, "The KeepAlive Url is : $url")
                    playerKeepAliveService.keepAliveURL = url
                    mediaEntry.sources[0].url = url
                }
                setMediaEntry()
            }
        } catch (e: MalformedURLException) {
            Log.d(TAG, "The KeepAlive Url is : " + e.message)
            e.printStackTrace()
        }
    }

    private fun onMediaLoaded() {
        loadPlayerSettings()
        setMediaEntry()
    }

    private fun setMediaEntry() {
        val mediaConfig = PKMediaConfig().setMediaEntry(mediaEntry)
        if (mediaEntry.mediaType == PKMediaEntry.MediaEntryType.Live) {
            mediaEntry.mediaType = PKMediaEntry.MediaEntryType.DvrLive
            playerControls.asset = asset
            //playerControls.disableControllersForLive();
        } else {
            mediaConfig.startPosition = 0L
        }
        player?.prepare(mediaConfig)
        player?.play()
    }

    private fun getKeepAliveHeaderUrl(url: URL, listener: ((status: Boolean, url: String) -> Unit)) {
        Thread(Runnable {
            try {
                val conn = url.openConnection() as HttpURLConnection
                conn.instanceFollowRedirects = false
                val keepAliveURL = conn.getHeaderField("Location")
                val isSuccess = !TextUtils.isEmpty(keepAliveURL) && conn.responseCode == 307
                if (isSuccess) {
                    requireActivity().runOnUiThread { listener(true, keepAliveURL) }
                } else {
                    val url1 = URL(keepAliveURL)
                    getKeepAliveHeaderUrl(url1, listener)
                }
            } catch (e: Exception) {
                listener(false, "Failed to retreive Location header : " + e.message)
                e.printStackTrace()
            }
        }).start()
    }

    private fun addPlayerListeners() {
        player?.let {
            it.addListener(this, PlayerEvent.tracksAvailable) { event: TracksAvailable? ->
                //When the track data available, this event occurs. It brings the info object with it.
                if (event?.tracksInfo != null && event.tracksInfo.textTracks.isNotEmpty()) {
                    val defaultTextTrack = getDefaultTextTrack(event.tracksInfo)
                    initSubtitles(event.tracksInfo.textTracks, defaultTextTrack)
                    changeTextTrack(defaultTextTrack)
                }
            }
            it.addListener(this, AdEvent.Type.CONTENT_PAUSE_REQUESTED) { playerControls.setPlayerState(PlayerState.READY) }
            it.addListener(this, PlayerEvent.pause) { }
            it.addListener(this, PlayerEvent.play) { if (isKeepAlive) playerKeepAliveService.startFireKeepAliveService() }
            it.addListener(this, PlayerEvent.stateChanged) { event: StateChanged -> playerControls.setPlayerState(event.newState) }
            it.addListener(this, PlayerEvent.Type.ERROR) { event: PKEvent? ->
                //When the track data available, this event occurs. It brings the info object with it.
                val playerError = event as PlayerEvent.Error?
                if (playerError?.error != null) {
                    toast("PlayerEvent.Error event  position = ${playerError.error.errorType} errorMessage = ${playerError.error.message}")
                }
            }
            //OLD WAY FOR GETTING THE CONCURRENCY
            it.addListener(this, OttEvent.OttEventType.Concurrency) { toast("Concurrency event") }
        }
    }

    private fun initSubtitles(tracks: List<TextTrack>, selected: TextTrack) {
        val languages = arrayListOf<String>()
        tracks.forEach {
            if (it.language != null) languages.add(it.language!!)
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subtitles.adapter = adapter
        subtitles.setSelection(tracks.indexOf(selected))
        subtitles.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View, position: Int, id: Long) {
                changeTextTrack(tracks[position])
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {}
        }
    }

    private fun changeTextTrack(textTrack: TextTrack) {
        player?.changeTrack(textTrack.uniqueId)
    }

    private fun getDefaultTextTrack(tracksInfo: PKTracks): TextTrack {
        var track = tracksInfo.textTracks[0]
        tracksInfo.textTracks.forEach {
            if (it?.language != null && it.language.equals("en", ignoreCase = true)) track = it
        }
        return track
    }

    private fun likeList() {
        withInternetConnection {
            val socialActionFilter = SocialActionFilter().apply { assetIdIn = asset!!.id.toString() }
            PhoenixApiManager.execute(SocialActionService.list(socialActionFilter).setCompletion {
                if (it.isSuccess) {
                    it.results.objects.forEach {
                        if (it.actionType == SocialActionType.LIKE) {
                            likeId = it.id
                            like.isChecked = true
                            return@forEach
                        }
                    }
                }
            })
            clearDebugView()
        }
    }

    private fun favoriteList() {
        withInternetConnection {
            val favoriteFilter = FavoriteFilter().apply { mediaIdIn = asset!!.id.toString() }
            PhoenixApiManager.execute(FavoriteService.list(favoriteFilter).setCompletion {
                if (it.isSuccess) {
                    if (it.results.objects != null && it.results.objects.isNotEmpty()) {
                        favorite.isChecked = true
                    }
                }
            })
            clearDebugView()
        }
    }

    private fun actionLike() {
        withInternetConnection {
            val requestBuilder = if (likeId.isEmpty()) {
                val socialAction = SocialAction().apply {
                    assetId = asset!!.id
                    actionType = SocialActionType.LIKE
                }

                SocialActionService.add(socialAction).setCompletion {
                    like.isEnabled = true
                    if (it.isSuccess) likeId = it.results.socialAction.id
                    else like.isChecked = false
                }
            } else {
                SocialActionService.delete(likeId).setCompletion {
                    like.isEnabled = true
                    if (it.isSuccess) likeId = ""
                    else like.isChecked = true
                }
            }
            PhoenixApiManager.execute(requestBuilder)
            clearDebugView()
            like.isEnabled = false
        }
    }

    private fun actionFavorite() {
        withInternetConnection {
            val requestBuilder = if (favorite.isChecked) {
                favorite.isEnabled = false
                val favoriteEntity = Favorite().apply { assetId = asset!!.id }
                FavoriteService.add(favoriteEntity).setCompletion {
                    favorite.isEnabled = true
                    if (!it.isSuccess) favorite.isChecked = false
                }
            } else {
                FavoriteService.delete(asset!!.id.toInt().toLong()).setCompletion {
                    favorite.isEnabled = true
                    if (!it.isSuccess) favorite.isChecked = true
                }
            }
            PhoenixApiManager.execute(requestBuilder)
            clearDebugView()
        }
    }

    private fun checkAllTogetherRequest() {
        withInternetConnection {
            if (TextUtils.isDigitsOnly(asset!!.id.toString())) {
                val multiRequestBuilder = MultiRequestBuilder()
                // product price request
                val productPriceFilter = ProductPriceFilter().apply { fileIdIn = asset!!.id.toString() }
                multiRequestBuilder.add(ProductPriceService.list(productPriceFilter))
                // bookmark request
                val bookmarkFilter = BookmarkFilter().apply {
                    assetIdIn = asset!!.id.toString()
                    assetTypeEqual = AssetType.MEDIA
                }

                multiRequestBuilder.add(BookmarkService.list(bookmarkFilter))
                // asset rules request
                val userAssetRuleFilter = UserAssetRuleFilter().apply {
                    assetTypeEqual = 1
                    assetIdEqual = asset!!.id
                }

                multiRequestBuilder.add(UserAssetRuleService.list(userAssetRuleFilter))
                multiRequestBuilder.setCompletion {
                    if (it.isSuccess) {
                        if (it.results != null && it.results[2] != null) {
                            val userAssetRules = (it.results[2] as ListResponse<UserAssetRule>).objects
                            userAssetRules.forEach {
                                if (it.ruleType == RuleType.PARENTAL) {
                                    parentalRuleId = it.id.toInt()
                                    pinLayout.visible()
                                }
                            }
                        }
                    }
                }
                PhoenixApiManager.execute(multiRequestBuilder)
                clearDebugView()
            } else {
                toast("Wrong input")
            }
        }
    }

    private fun checkPinRequest(pin: String) {
        withInternetConnection {
            if (TextUtils.isDigitsOnly(pin)) {
                PhoenixApiManager.execute(PinService.validate(pin, PinType.PARENTAL, parentalRuleId))
                clearDebugView()
            } else {
                toast("Wrong input")
            }
        }
    }

    private fun showPinInput() {
        pinInputLayout.visible()
        insertPin.text = "Check pin"
        Utils.showKeyboard(pin)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isKeepAlive) {
            playerKeepAliveService.cancelFireKeepAliveService()
            isKeepAlive = false
        }
        Utils.hideKeyboard(view)
        PhoenixApiManager.cancelAll()
    }

    override fun onPause() {
        super.onPause()
        playerControls.release()
        player?.onApplicationPaused()
    }

    override fun onResume() {
        super.onResume()
        player?.onApplicationResumed()
        playerControls.resume()
    }
}