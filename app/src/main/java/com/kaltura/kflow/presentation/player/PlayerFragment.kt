package com.kaltura.kflow.presentation.player

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.core.view.isGone
import androidx.navigation.fragment.navArgs
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
import com.kaltura.playkit.plugins.kava.KavaAnalyticsConfig
import com.kaltura.playkit.plugins.kava.KavaAnalyticsPlugin
import com.kaltura.playkit.plugins.ott.OttEvent
import com.kaltura.playkit.providers.ovp.KalturaOvpMediaProvider
import kotlinx.android.synthetic.main.fragment_player.*
import org.jetbrains.anko.support.v4.runOnUiThread
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 04.12.2018.
 */
class PlayerFragment : DebugFragment(R.layout.fragment_player) {

    private val viewModel: PlayerViewModel by viewModel()

    private val KAVA_BASE_URL = "https://analytics.kaltura.com/api_v3/index.php"

    // UIConf id -- optional for KAVA
    private val UI_CONF_ID = 0
    private val TAG = PlayerFragment::class.java.canonicalName
    private val args: PlayerFragmentArgs by navArgs()
    private lateinit var player: Player
    private var likeId = ""
    private var parentalRuleId = 0

    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        onAssetLoaded()
    }

    private fun initUI() {
        like.setOnCheckedChangeListener { _, _ ->
            if (like.isPressed) actionLike()
        }
        favorite.setOnCheckedChangeListener { _, _ ->
            if (favorite.isPressed) actionFavorite()
        }
        playerControls.setOnStartOverClickListener(View.OnClickListener {
//            if (pkMediaEntry.mediaType == PKMediaEntry.MediaEntryType.Vod) player?.replay()
//            else if (asset is ProgramAsset && (asset as ProgramAsset).isProgramInPast()) initPlayer(APIDefines.PlaybackContextType.Catchup)
//            else if (asset is ProgramAsset && (asset as ProgramAsset).isProgramInLive()) initPlayer(APIDefines.PlaybackContextType.StartOver)
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
//        observeResource(viewModel.mediaEntry) {
//            mediaEntry = it
//            onAssetLoaded()
//        }
//        observeResource(viewModel.userAssetRules) {
//            it.forEach {
//                if (it.ruleType == RuleType.PARENTAL) {
//                    parentalRuleId = it.id.toInt()
//                    pinLayout.visible()
//                }
//            }
//        }
//        observeResource(viewModel.favoriteList) { favorite.isChecked = true }
//        observeResource(viewModel.getLike) {
//            likeId = it.id
//            like.isChecked = true
//        }
//        observeResource(viewModel.doLike, error = {
//            like.isEnabled = true
//            like.isChecked = false
//        }, success = {
//            like.isEnabled = true
//            likeId = it.id
//        })
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

//    private fun loadAsset(mediaEntryId: String) {
//        withInternetConnection {
//            clearDebugView()
//            viewModel.loadAsset(mediaEntryId)
//        }
//    }

    private fun onAssetLoaded() {
        assetTitle.text = args.mediaEntry.name ?: ""
        registerPlugins()
        setupPlayer()
        likeList()
        favoriteList()
        loadMedia()
    }

    private fun setupPlayer() {
        player = PlayKitManager.loadPlayer(requireContext(), createPluginConfigs()).apply {
            settings.setSecureSurface(false)
            settings.setAllowCrossProtocolRedirect(true)
            settings.setCea608CaptionsEnabled(true) // default is false
        }
        playerLayout.addView(player.view)
        addPlayerListeners()
        playerControls.player = player
    }

    private fun createPluginConfigs() = PKPluginConfigs().apply {
        setPluginConfig(KavaAnalyticsPlugin.factory.name, getKavaConfig())
    }

    private fun registerPlugins() {
        PlayKitManager.registerPlugins(requireContext(), KavaAnalyticsPlugin.factory)
    }

    private fun getKavaConfig() = KavaAnalyticsConfig()
            .setBaseUrl(KAVA_BASE_URL)
            .setPartnerId(viewModel.getPartnerId())
            .setEntryId(args.mediaEntry.id)
            .setKs(viewModel.getKs())
            .setUiConfId(UI_CONF_ID)

    private fun loadMedia() {
        KalturaOvpMediaProvider(viewModel.getBaseUrl(), viewModel.getPartnerId(), viewModel.getKs())
                .setEntryId(args.mediaEntry.id)
                .load { response ->
                    runOnUiThread {
                        if (response.isSuccess) {
                            // Update with entryId and KS
                            player.updatePluginConfig(KavaAnalyticsPlugin.factory.name, getKavaConfig())
                            playerControls.mediaEntry = args.mediaEntry
                            player.prepare(PKMediaConfig().setMediaEntry(response.response))
                            player.play() // Will play when ready
                        } else {
                            toast("Failed loading media: " + response.error)
                        }
                    }
                }
    }

    private fun addPlayerListeners() {
        player.addListener(this, PlayerEvent.tracksAvailable) { event: TracksAvailable? ->
            //When the track data available, this event occurs. It brings the info object with it.
            if (event?.tracksInfo != null && event.tracksInfo.textTracks.isNotEmpty()) {
                val defaultTextTrack = getDefaultTextTrack(event.tracksInfo)
                initSubtitles(event.tracksInfo.textTracks, defaultTextTrack)
                changeTextTrack(defaultTextTrack)
            }
        }
        player.addListener(this, AdEvent.Type.CONTENT_PAUSE_REQUESTED) { playerControls.setPlayerState(PlayerState.READY) }
        player.addListener(this, PlayerEvent.pause) { }
        player.addListener(this, PlayerEvent.play) { }
        player.addListener(this, PlayerEvent.stateChanged) { event: StateChanged -> playerControls.setPlayerState(event.newState) }
        player.addListener(this, PlayerEvent.Type.ERROR) { event: PKEvent? ->
            //When the track data available, this event occurs. It brings the info object with it.
            val playerError = event as PlayerEvent.Error?
            if (playerError?.error != null) {
                toast("PlayerEvent.Error event  position = ${playerError.error.errorType} errorMessage = ${playerError.error.message}")
            }
        }
        //OLD WAY FOR GETTING THE CONCURRENCY
        player.addListener(this, OttEvent.OttEventType.Concurrency) { toast("Concurrency event") }
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
        player.changeTrack(textTrack.uniqueId)
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
//            viewModel.getLike(asset!!.id)
        }
    }

    private fun favoriteList() {
        withInternetConnection {
            clearDebugView()
//            viewModel.getFavoriteList(asset!!.id)
        }
    }

    private fun actionLike() {
        withInternetConnection {
            clearDebugView()
            like.isEnabled = false
//            if (likeId.isEmpty()) viewModel.like(asset!!.id)
//            else viewModel.unlike(likeId)
        }
    }

    private fun actionFavorite() {
        withInternetConnection {
            clearDebugView()
            favorite.isEnabled = false
//            if (favorite.isChecked) viewModel.favorite(asset!!.id)
//            else viewModel.unfavorite(asset!!.id)
        }
    }

    private fun checkAllTogetherRequest() {
        withInternetConnection {
            clearDebugView()
//                viewModel.checkAllValidations(asset!!.id)
        }
    }

    private fun checkPinRequest(pin: String) {
        withInternetConnection {
            if (TextUtils.isDigitsOnly(pin)) {
                clearDebugView()
                viewModel.checkPinCode(pin, parentalRuleId)
            } else {
                toast("Wrong input")
            }
        }
    }

    private fun showPinInput() {
        pinInputLayout.visible()
        insertPin.text = "Check pin"
        showKeyboard(pin)
    }

    override fun onDestroyView() {
        player.destroy()
        super.onDestroyView()
    }

    override fun onPause() {
        playerControls.release()
        player.onApplicationPaused()
        super.onPause()
    }

    override fun onResume() {
        player.onApplicationResumed()
        playerControls.resume()
        super.onResume()
    }
}