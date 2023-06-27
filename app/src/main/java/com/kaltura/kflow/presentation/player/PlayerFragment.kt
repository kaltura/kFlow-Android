package com.kaltura.kflow.presentation.player

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.kaltura.client.enums.*
import com.kaltura.client.types.*
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.playkit.*
import com.kaltura.playkit.PlayerEvent.StateChanged
import com.kaltura.playkit.PlayerEvent.TracksAvailable
import com.kaltura.playkit.player.PKTracks
import com.kaltura.playkit.player.TextTrack
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ott.OttEvent
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsConfig
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsPlugin
import com.kaltura.playkit.plugins.youbora.YouboraPlugin
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.api.phoenix.APIDefines.KalturaAssetType
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.*
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_ACCOUNT_CODE
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_APP_NAME
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_APP_RELEASE_VERSION
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_ENABLED
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_USERNAME
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/**
 * Created by alex_lytvynenko on 04.12.2018.
 */
class PlayerFragment : DebugFragment(R.layout.fragment_player) {

    private val viewModel: PlayerViewModel by viewModel()

    private val TAG = PlayerFragment::class.java.canonicalName
    private val args: PlayerFragmentArgs by navArgs()
    private var player: KalturaPlayer? = null
    private var asset: Asset? = null
    private var likeId = ""
    private lateinit var mediaEntry: PKMediaEntry
    private var parentalRuleId = 0
    private var isKeepAlive = false
    private var playerKeepAliveService = PlayerKeepAliveService()
    private val initialPlaybackContextType by lazy { playbackContextTypeFromString(args.playbackContextType) }

    override fun debugView(): DebugView = debugView

    //Youbora analytics Constants
    val ACCOUNT_CODE = "reshet"
    val UNIQUE_USER_NAME = "elad"
    val MEDIA_TITLE = "Reshet13"
//    val ENABLE_SMART_ADS = true
//    private val CAMPAIGN = "your_campaign_name"
//    val EXTRA_PARAM_1 = "playKitPlayer"
//    val EXTRA_PARAM_2 = ""
//    val GENRE = "your_genre"
//    val TYPE = "your_type"
//    val TRANSACTION_TYPE = "your_trasnsaction_type"
//    val YEAR = "your_year"
//    val CAST = "your_cast"
//    val DIRECTOR = "your_director"
//    private val OWNER = "your_owner"
//    val PARENTAL = "your_parental"
//    val PRICE = "your_price"
//    val RATING = "your_rating"
//    val AUDIO_TYPE = "your_audio_type"
//    val AUDIO_CHANNELS = "your_audoi_channels"
//    val DEVICE = "your_device"
//    val QUALITY = "your_quality"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        KalturaOttPlayer.initialize(requireContext(), viewModel.getPartnerId(), viewModel.getBaseUrl() + "/api_v3/")

        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        initUI()
        asset = args.asset
        isKeepAlive = args.isKeepAlive
        if (asset == null && args.recording != null) loadAsset(args.recording!!.assetId) else onAssetLoaded()
    }

    private fun initUI() {
        like.setOnCheckedChangeListener { _, _ ->
            if (like.isPressed) actionLike()
        }
        favorite.setOnCheckedChangeListener { _, _ ->
            if (favorite.isPressed) actionFavorite()
        }
        playerControls.setOnStartOverClickListener {
            if (mediaEntry.mediaType == PKMediaEntry.MediaEntryType.Vod) player?.replay()
            else if (asset is ProgramAsset && (asset as ProgramAsset).isProgramInPast()) initPlayer(
                APIDefines.PlaybackContextType.Catchup
            )
            else if (asset is ProgramAsset && (asset as ProgramAsset).isProgramInLive()) initPlayer(
                APIDefines.PlaybackContextType.StartOver
            )
        }
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
        initPlayer(getPlaybackContextType())
        likeList()
        favoriteList()
    }

    private fun initPlayer(playbackContextType: APIDefines.PlaybackContextType) {
        loadPlayer(playbackContextType) { mediaOptions, entry, loadError ->
            if (isAdded) {
                requireActivity().runOnUiThread {
                    if (entry != null) {
                        mediaEntry = entry
                        if (isKeepAlive) onMediaLoadedKeepAlive()
                        else setMediaEntry()
                    } else {
                        toast("failed to fetch media data: ${loadError?.message}")
                    }
                }
            }
        }
    }

    private fun configurePlugins(pluginConfigs: PKPluginConfigs) {
        addPhoenixAnalyticsPluginConfig(pluginConfigs)
        addYouboraPluginConfig(pluginConfigs)
    }

    private fun addPhoenixAnalyticsPluginConfig(config: PKPluginConfigs) {
        val ks = viewModel.getKs()
        val pId = viewModel.getPartnerId()
        val baseUrl = viewModel.getBaseUrl() + "/api_v3/"
        val phoenixAnalyticsConfig = PhoenixAnalyticsConfig(pId, baseUrl, ks, 30)
        config.setPluginConfig(PhoenixAnalyticsPlugin.factory.name, phoenixAnalyticsConfig)
    }

    private fun addYouboraPluginConfig(config: PKPluginConfigs) {

//        val phoenixYouboraConfig = YouboraPlugin()
        config.setPluginConfig(YouboraPlugin.factory.name, getYouboraBundle())
    }

    private fun getYouboraBundle(): Bundle {

        val optBundle = Bundle()

        //Youbora config bundle. Main config goes here.
        optBundle.putString(KEY_ACCOUNT_CODE, "reshet")
        optBundle.putString(KEY_USERNAME, "reshet")
        optBundle.putBoolean(KEY_ENABLED, true)
        optBundle.putString(KEY_APP_NAME, "Reshet13");
        optBundle.putString(KEY_APP_RELEASE_VERSION, "v1.0");

        //Media entry bundle.
//        optBundle.putString(KEY_CONTENT_TITLE, MEDIA_TITLE)
//
//        //Optional - Device bundle o/w youbora will decide by its own.
//        optBundle.putString(KEY_DEVICE_CODE, "AndroidTV")
//        optBundle.putString(KEY_DEVICE_BRAND, "Xiaomi")
//        optBundle.putString(KEY_DEVICE_MODEL, "Mii3")
//        optBundle.putString(KEY_DEVICE_TYPE, "TvBox")
//        optBundle.putString(KEY_DEVICE_OS_NAME, "Android/Oreo")
//        optBundle.putString(KEY_DEVICE_OS_VERSION, "8.1")
//
//        //Youbora ads configuration bundle.
//        optBundle.putString(KEY_AD_CAMPAIGN, CAMPAIGN)
//
//        //Configure custom properties here:
//        optBundle.putString(KEY_CONTENT_GENRE, GENRE)
//        optBundle.putString(KEY_CONTENT_TYPE, TYPE)
//        optBundle.putString(KEY_CONTENT_TRANSACTION_CODE, TRANSACTION_TYPE)
//
//        optBundle.putString(KEY_CONTENT_PRICE, PRICE)
//        optBundle.putString(KEY_CONTENT_ENCODING_AUDIO_CODEC, AUDIO_TYPE)
//        optBundle.putString(KEY_CONTENT_CHANNEL, AUDIO_CHANNELS)
//
//        val contentMetadataBundle = Bundle()
//
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_YEAR, YEAR)
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_CAST, CAST)
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_DIRECTOR, DIRECTOR)
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_OWNER, OWNER)
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_PARENTAL, PARENTAL)
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_RATING, RATING)
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_QUALITY, QUALITY)
//
//        optBundle.putBundle(KEY_CONTENT_METADATA, contentMetadataBundle)
//
//        //You can add some extra params here:
//        optBundle.putString(KEY_CUSTOM_DIMENSION_1, EXTRA_PARAM_1)
//        optBundle.putString(KEY_CUSTOM_DIMENSION_2, EXTRA_PARAM_2)

        return optBundle
    }


    private fun buildOttMediaOptions(playbackContextType: APIDefines.PlaybackContextType): OTTMediaOptions {
        val ottMediaAsset = OTTMediaAsset()
            .setAdapterData(
                hashMapOf(
                    "codec" to viewModel.codec,
                    "DRM" to viewModel.drm.toString(),
                    "quality" to viewModel.quality,
                )
            )
            .setAssetId(getAssetIdByFlowType())
            .setContextType(playbackContextType)
            .setAssetReferenceType(getAssetReferenceType(playbackContextType))
            .setAssetType(getAssetType(playbackContextType))
            .setFormats(listOf(viewModel.getMediaFileFormat())).apply {
                when (viewModel.urlType) {
                    APIDefines.KalturaUrlType.Direct.value -> urlType =
                        APIDefines.KalturaUrlType.Direct
                    APIDefines.KalturaUrlType.Direct.value -> urlType =
                        APIDefines.KalturaUrlType.Direct
                    else -> Unit
                }
                when (viewModel.streamerType) {
                    APIDefines.KalturaStreamerType.Mpegdash.value -> streamerType =
                        APIDefines.KalturaStreamerType.Mpegdash
                    APIDefines.KalturaStreamerType.Multicast.value -> streamerType =
                        APIDefines.KalturaStreamerType.Multicast
                    else -> Unit
                }
                protocol = when (viewModel.mediaProtocol) {
                    PhoenixMediaProvider.HttpProtocol.Http -> PhoenixMediaProvider.HttpProtocol.Http
                    PhoenixMediaProvider.HttpProtocol.Https -> PhoenixMediaProvider.HttpProtocol.Https
                    else -> PhoenixMediaProvider.HttpProtocol.All
                }
            }
            .setKs(viewModel.getKs())
            .setUrlType(APIDefines.KalturaUrlType.Direct)
            .setStreamerType(APIDefines.KalturaStreamerType.Mpegdash)
        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = args.startPosition.toLong()

        return ottMediaOptions
    }

    private fun getAssetIdByFlowType(): String = when {
        args.recording != null -> args.recording!!.id.toString()
        (asset is ProgramAsset && getPlaybackContextType() == APIDefines.PlaybackContextType.Playback) -> (asset as ProgramAsset).linearAssetId.toString()
        else -> asset!!.id.toString()
    }

    private fun getAssetType(playbackContextType: APIDefines.PlaybackContextType): KalturaAssetType =
        when {
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

    private fun getAssetReferenceType(playbackContextType: APIDefines.PlaybackContextType): APIDefines.AssetReferenceType =
        when {
            playbackContextType == APIDefines.PlaybackContextType.StartOver || playbackContextType == APIDefines.PlaybackContextType.Catchup -> APIDefines.AssetReferenceType.InternalEpg
            args.recording == null -> APIDefines.AssetReferenceType.Media
            else -> APIDefines.AssetReferenceType.Npvr
        }

    private fun loadPlayer(
        playbackContextType: APIDefines.PlaybackContextType,
        completion: KalturaPlayer.OnEntryLoadListener
    ) {
        if (player == null) {
            val playerInitOptions = PlayerInitOptions(viewModel.getPartnerId())
                .setPKRequestConfig(PKRequestConfig(true))
                .setSecureSurface(false)

            val pluginConfig = PKPluginConfigs()
            configurePlugins(pluginConfig)
            playerInitOptions.setPluginConfigs(pluginConfig)
            player = KalturaOttPlayer.create(requireContext(), playerInitOptions)

            addPlayerListeners()
            player!!.setPlayerView(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            playerLayout.addView(player?.playerView)

            val ottMediaOptions = buildOttMediaOptions(playbackContextType)
            player!!.loadMedia(ottMediaOptions, completion)

            playerControls.player = player
            playerControls.asset = asset
        }
    }

    private fun onMediaLoadedKeepAlive() {
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

    private fun setMediaEntry() {
        if (mediaEntry.mediaType == PKMediaEntry.MediaEntryType.Live) {
            mediaEntry.mediaType = PKMediaEntry.MediaEntryType.DvrLive
            playerControls.asset = asset
            //playerControls.disableControllersForLive();
        }

        player?.play()
    }

    private fun getKeepAliveHeaderUrl(
        url: URL,
        listener: ((status: Boolean, url: String) -> Unit)
    ) {
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
            it.addListener(
                this,
                AdEvent.Type.CONTENT_PAUSE_REQUESTED
            ) { playerControls.setPlayerState(PlayerState.READY) }
            it.addListener(this, PlayerEvent.pause) { }
            it.addListener(
                this,
                PlayerEvent.play
            ) { if (isKeepAlive) playerKeepAliveService.startFireKeepAliveService() }
            it.addListener(
                this,
                PlayerEvent.stateChanged
            ) { event: StateChanged -> playerControls.setPlayerState(event.newState) }
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
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        subtitles.adapter = adapter
        subtitles.setSelection(tracks.indexOf(selected))
        subtitles.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View,
                position: Int,
                id: Long
            ) {
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