package com.kaltura.kflow.presentation.player

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.kaltura.client.enums.AssetIndexStatus
import com.kaltura.client.enums.RuleType
import com.kaltura.client.types.Asset
import com.kaltura.client.types.ProgramAsset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
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
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.*

/**
 * Created by alex_lytvynenko on 04.12.2018.
 */
class PlayerFragment : DebugFragment(R.layout.fragment_player) {

    private val viewModel: PlayerViewModel by viewModel()
    private val TAG = PlayerFragment::class.java.canonicalName
    private val args: PlayerFragmentArgs by navArgs()
    private var player: Player? = null
    private var asset: Asset? = null
    private var likeId = ""
    private var isHdmiConnected = false
    private lateinit var mediaEntry: PKMediaEntry
    private var parentalRuleId = 0
    private var isKeepAlive = false
    private var playerKeepAliveService = PlayerKeepAliveService()
    private val initialPlaybackContextType by lazy { playbackContextTypeFromString(args.playbackContextType) }

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        initUI()
        asset = args.asset
        isKeepAlive = args.isKeepAlive
        if (asset == null && args.recording != null) loadAsset(args.recording!!.assetId) else onAssetLoaded()
        hdmiLogic()
    }

    private fun hdmiLogic(){
        val mDisplayListener: DisplayManager.DisplayListener = object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) {
                Log.d(TAG, "Display #$displayId added.")
                isHdmiConnected = true
                player?.stop()
            }

            override fun onDisplayChanged(displayId: Int) {

            }

            override fun onDisplayRemoved(displayId: Int) {
                Log.d(TAG, "Display #$displayId removed.")
                isHdmiConnected = false
                onAssetLoaded()
            }
        }
        val displayManager = activity?.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        displayManager?.registerDisplayListener(mDisplayListener,null)
        if (displayManager != null) {
            isHDMI(displayManager)
        }
    }

    private fun isHDMI(displayManager :DisplayManager ){

        val outPoint = getScreenSize()

        val iterator = displayManager?.getDisplays()?.iterator()

        // do something with the rest of elements
        iterator?.forEach {
            Log.d(TAG, "Display Name is #${it.name}")
            Log.d(TAG, "Display State is #${it.state}")
            Log.d(TAG, "Display ScreenHeight is #${it.getSize(outPoint)}")

            if (it.name.contains("HDMI", true)) {
                Log.d(TAG, "HDMI Was Detected, Stopping The Player")
                isHdmiConnected = true
                player?.stop()
            }

        }

    }

    fun getScreenSize(): Point? {
        val display: Display = activity?.getWindowManager()?.getDefaultDisplay()!!
        val size = Point()
        if (Build.VERSION.SDK_INT >= 13) {
            display.getSize(size)
        } else {
            size[display.getWidth()] = display.getHeight()
        }
        return size
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
            else if (asset is ProgramAsset && (asset as ProgramAsset).isProgramInPast()) initPlayer(APIDefines.PlaybackContextType.Catchup)
            else if (asset is ProgramAsset && (asset as ProgramAsset).isProgramInLive()) initPlayer(APIDefines.PlaybackContextType.StartOver)
        })
        checkAll.setOnClickListener {
            hideKeyboard()
            checkAllTogetherRequest()
        }
        insertPin.setOnClickListener {
            hideKeyboard()
            if (pinInputLayout.isGone) showPinInput()
            else checkPinRequest(pin.string)
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.asset) {
            asset = it
            if (it.indexStatus == AssetIndexStatus.DELETED) toast("Asset was deleted!")
            else onAssetLoaded()
        }
        observeResource(viewModel.userAssetRules,
                error = { checkAll.error(lifecycleScope) },
                success = {
                    checkAll.success(lifecycleScope)
                    it.forEach {
                        if (it.ruleType == RuleType.PARENTAL) {
                            parentalRuleId = it.id.toInt()
                            pinLayout.visible()
                        }
                    }
                })
        observeResource(viewModel.favoriteList) { favorite.isChecked = true }
        observeResource(viewModel.getLike) {
            likeId = it.id
            like.isChecked = true
        }
        observeResource(viewModel.doLike, error = {
            like.isEnabled = true
            like.isChecked = false
        }, success = {
            like.isEnabled = true
            likeId = it.id
        })
        observeResource(viewModel.doUnlike, error = {
            like.isEnabled = true
            like.isChecked = true
        }, success = {
            like.isEnabled = true
            likeId = ""
        })
        observeResource(viewModel.doFavorite, error = {
            favorite.isEnabled = true
            favorite.isChecked = false
        }, success = {
            favorite.isEnabled = true
        })
        observeResource(viewModel.doUnfavorite, error = {
            favorite.isEnabled = true
            favorite.isChecked = true
        }, success = {
            favorite.isEnabled = true
        })
    }

    private fun loadAsset(assetId: Long) {
        withInternetConnection {
            clearDebugView()
            viewModel.loadAsset(assetId)
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
        val ks = viewModel.getKs()
        val pId = viewModel.getPartnerId()
        val baseUrl = viewModel.getBaseUrl() + "/api_v3/"
        val phoenixAnalyticsConfig = PhoenixAnalyticsConfig(pId, baseUrl, ks, 30)
        config.setPluginConfig(PhoenixAnalyticsPlugin.factory.name, phoenixAnalyticsConfig)
    }

    private fun startOttMediaLoading(playbackContextType: APIDefines.PlaybackContextType, completion: OnMediaLoadCompletion) {
        val mediaProvider: MediaEntryProvider = PhoenixMediaProvider()
                .setSessionProvider(SimpleSessionProvider(viewModel.getBaseUrl() + "/api_v3/", viewModel.getPartnerId(), viewModel.getKs()))
                .setAssetId(getAssetIdByFlowType())
                .setContextType(playbackContextType)
                .setAssetReferenceType(getAssetReferenceType(playbackContextType))
                .setAssetType(getAssetType(playbackContextType))
                .setFormats(viewModel.getMediaFileFormat()).apply {
                    when (viewModel.urlType) {
                        APIDefines.KalturaUrlType.Direct.value -> setPKUrlType(APIDefines.KalturaUrlType.Direct)
                        APIDefines.KalturaUrlType.PlayManifest.value -> setPKUrlType(APIDefines.KalturaUrlType.PlayManifest)
                        else -> Unit
                    }
                    when (viewModel.streamerType) {
                        APIDefines.KalturaStreamerType.Mpegdash.value -> setPKStreamerType(APIDefines.KalturaStreamerType.Mpegdash)
                        else -> Unit
                    }
                    when (viewModel.mediaProtocol) {
                        PhoenixMediaProvider.HttpProtocol.Http -> setProtocol(PhoenixMediaProvider.HttpProtocol.Http)
                        PhoenixMediaProvider.HttpProtocol.Https -> setProtocol(PhoenixMediaProvider.HttpProtocol.Https)
                        else -> setProtocol(PhoenixMediaProvider.HttpProtocol.All)
                    }
                }

        mediaProvider.load(completion)
    }

    private fun getAssetIdByFlowType(): String = when {
        args.recording != null -> args.recording!!.id.toString()
        (asset is ProgramAsset && getPlaybackContextType() == APIDefines.PlaybackContextType.Playback) -> (asset as ProgramAsset).linearAssetId.toString()
        else -> asset!!.id.toString()
    }

    private fun getAssetType(playbackContextType: APIDefines.PlaybackContextType): KalturaAssetType = when {
        args.recording != null -> KalturaAssetType.Recording
        (playbackContextType == APIDefines.PlaybackContextType.StartOver || playbackContextType == APIDefines.PlaybackContextType.Catchup) -> KalturaAssetType.Epg
        else -> KalturaAssetType.Media
    }

    private fun getPlaybackContextType(): APIDefines.PlaybackContextType = when {
        initialPlaybackContextType != null -> initialPlaybackContextType!!
        args.recording != null -> APIDefines.PlaybackContextType.Playback
        asset is ProgramAsset && (asset as ProgramAsset).isProgramInPast() -> APIDefines.PlaybackContextType.Catchup
        asset is ProgramAsset && (asset as ProgramAsset).isProgramInLive() -> APIDefines.PlaybackContextType.Playback
        else -> APIDefines.PlaybackContextType.Playback
    }

    private fun getAssetReferenceType(playbackContextType: APIDefines.PlaybackContextType): APIDefines.AssetReferenceType = when {
        playbackContextType == APIDefines.PlaybackContextType.StartOver || playbackContextType == APIDefines.PlaybackContextType.Catchup -> APIDefines.AssetReferenceType.InternalEpg
        args.recording == null -> APIDefines.AssetReferenceType.Media
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
            mediaConfig.startPosition = args.startPosition.toLong()
        }

        if (!isHdmiConnected) {
            player?.prepare(mediaConfig)
            player?.play()
        }else{
            player?.stop()
        }
    }

    private fun getKeepAliveHeaderUrl(url: URL, listener: ((status: Boolean, url: String) -> Unit)) {
        Thread {
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
        }.start()
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
            clearDebugView()
            viewModel.getLike(asset!!.id)
        }
    }

    private fun favoriteList() {
        withInternetConnection {
            clearDebugView()
            viewModel.getFavoriteList(asset!!.id)
        }
    }

    private fun actionLike() {
        withInternetConnection {
            clearDebugView()
            like.isEnabled = false
            if (likeId.isEmpty()) viewModel.like(asset!!.id)
            else viewModel.unlike(likeId)
        }
    }

    private fun actionFavorite() {
        withInternetConnection {
            clearDebugView()
            favorite.isEnabled = false
            if (favorite.isChecked) viewModel.favorite(asset!!.id)
            else viewModel.unfavorite(asset!!.id)
        }
    }

    private fun checkAllTogetherRequest() {
        withInternetConnection {
            clearDebugView()
            if (TextUtils.isDigitsOnly(asset!!.id.toString()).not()) {
                toast("Error")
                return@withInternetConnection
            }

            checkAll.startAnimation {
                viewModel.checkAllValidations(asset!!.id)
            }
        }
    }

    private fun checkPinRequest(pin: String) {
        withInternetConnection {
            clearDebugView()
            pinInputLayout.hideError()
            if (TextUtils.isDigitsOnly(pin).not()) {
                pinInputLayout.showError("Wrong input")
                return@withInternetConnection
            }

            viewModel.checkPinCode(pin, parentalRuleId)
        }
    }

    private fun showPinInput() {
        pinInputLayout.visible()
        insertPin.text = "Check pin"
        showKeyboard(pin)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (isKeepAlive) {
            playerKeepAliveService.cancelFireKeepAliveService()
            isKeepAlive = false
        }
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