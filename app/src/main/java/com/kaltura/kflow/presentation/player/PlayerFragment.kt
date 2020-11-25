package com.kaltura.kflow.presentation.player

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.kaltura.client.enums.RuleType
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.debug.DebugView
import com.kaltura.kflow.presentation.extension.*
import com.kaltura.playkit.BuildConfig
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKPluginConfigs
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.plugins.kava.KavaAnalyticsConfig
import com.kaltura.playkit.plugins.kava.KavaAnalyticsPlugin
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import com.kaltura.tvplayer.config.PhoenixTVPlayerParams
import kotlinx.android.synthetic.main.fragment_player.*
import kotlinx.android.synthetic.main.view_bottom_debug.*
import org.jetbrains.anko.support.v4.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by alex_lytvynenko on 04.12.2018.
 */
class PlayerFragment : DebugFragment(R.layout.fragment_player) {

    private val viewModel: PlayerViewModel by viewModel()

    private val TAG = PlayerFragment::class.java.canonicalName
    private val args: PlayerFragmentArgs by navArgs()
    private var likeId = ""
    private lateinit var mediaEntry: PKMediaEntry
    private var parentalRuleId = 0
    private var isKeepAlive = false
    private var playerKeepAliveService = PlayerKeepAliveService()
    private val initialPlaybackContextType by lazy { playbackContextTypeFromString(args.playbackContextType) }

    private var frontPlayer: KalturaPlayer? = null
    private var backgroundplayer: KalturaPlayer? = null
    private var mediaEntryForBGPlayer: PKMediaEntry? = null
    private var playerAlignment: Int = 0 // 0: Front Player 1: Background Player
    private val FRONT_ALIGNMENT = 0
    private val BACKGROUND_ALIGNMENT = 1
    private var firstPlayback: Boolean = true
    private val cutOffTime: Long = 60 * 1000

    private var playerInitOptions: PlayerInitOptions? = null
    private var pkPluginConfigs = PKPluginConfigs()

    private lateinit var mediaIdOne: String
    private lateinit var fileIdOne: String
    private lateinit var mediaIdTwo: String
    private lateinit var fileIdTwo: String
    private lateinit var ks: String
    private var OTT_PARTNER_ID_POC: Int? = 0


    override fun debugView(): DebugView = debugView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

        mediaIdOne = args.mediaIdOne
        fileIdOne = args.fileIdOne
        mediaIdTwo = args.mediaIdTwo
        fileIdTwo = args.fileIdTwo

        ks = viewModel.getKs()!!
        OTT_PARTNER_ID_POC = viewModel.getPartnerId()

        isKeepAlive = args.isKeepAlive
        configurePlugins()

        firstPlayback = true
        Log.d(TAG, "POC Loading Front Player Very first time")
        loadKalturaPlayer(OTT_PARTNER_ID_POC, KalturaPlayer.Type.ott, pkPluginConfigs, FRONT_ALIGNMENT)
    }

    private fun configurePlugins() {
        pkPluginConfigs.setPluginConfig(KavaAnalyticsPlugin.factory.name, OTT_PARTNER_ID_POC?.let { getKavaAnalyticsConfig(it) })
    }

    private fun getKavaAnalyticsConfig(partnerId: Int): KavaAnalyticsConfig {
        return KavaAnalyticsConfig()
                .setApplicationVersion(BuildConfig.VERSION_NAME)
                .setPartnerId(partnerId)
                .setUserId("aaa@gmail.com")
                .setCustomVar1("Test1")
                .setApplicationVersion("Test123")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isKeepAlive) {
            playerKeepAliveService.cancelFireKeepAliveService()
            isKeepAlive = false
        }
    }

    override fun subscribeUI() {
        observeResource(viewModel.asset) {
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

    override fun onPause() {
        super.onPause()
        playerControls.release()
        frontPlayer?.let {
            if (it.isPlaying) {
                it.onApplicationPaused()
            }
        }

        backgroundplayer?.let {
            if (it.isPlaying) {
                it.onApplicationPaused()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (playerAlignment == BACKGROUND_ALIGNMENT) {
            frontPlayer?.onApplicationResumed()
        } else {
            backgroundplayer?.onApplicationResumed()
        }

        playerControls.resume()
    }

    /**
     * Load KalturaPlayer only for OVP and OTT provider ( User loadBasicKalturaPlayer() method to use the Basic
     * KalturaPlayer preparation )
     *
     * @param mediaPartnerId Partner ID for OVP or OTT provider
     * @param playerType OVP or OTT < KalturaPlayer.Type >
     * @param pkPluginConfigs Plugin configs (Configurations like IMA Ads, Youbora etc)
     * for Kaltura Player, it is being passed in playerInitOptions
     */

    fun loadKalturaPlayer(mediaPartnerId: Int?, playerType: KalturaPlayer.Type, pkPluginConfigs: PKPluginConfigs, playerAlignment: Int) {
        Log.d(TAG, "POC Loading Kaltura Player: playerAlignment => + $playerAlignment")
        playerInitOptions = PlayerInitOptions(mediaPartnerId)

        if (playerAlignment == BACKGROUND_ALIGNMENT) {
            playerInitOptions?.setAutoPlay(false)
            playerInitOptions?.setPreload(false)
        } else {
            playerInitOptions?.setAutoPlay(true)
            playerInitOptions?.setPreload(true)
        }
        playerInitOptions?.setSecureSurface(true)
        playerInitOptions?.setAdAutoPlayOnResume(true)
        playerInitOptions?.setAllowCrossProtocolEnabled(true)
        playerInitOptions?.setReferrer("app://MyApplicationDomain")
        // playerInitOptions.setLoadControlBuffers(new LoadControlBuffers());

        if (mediaPartnerId == 225) {
            val phoenixTVPlayerParams = PhoenixTVPlayerParams()
            phoenixTVPlayerParams.analyticsUrl = "https://analytics.kaltura.com"
            phoenixTVPlayerParams.ovpPartnerId = 1982551
            phoenixTVPlayerParams.partnerId = 225
            phoenixTVPlayerParams.serviceUrl = "https://rest-as.ott.kaltura.com/v5_2_8/"
            phoenixTVPlayerParams.ovpServiceUrl = "http://cdnapi.kaltura.com/"
            playerInitOptions?.tvPlayerParams = phoenixTVPlayerParams
        }

        playerInitOptions?.setPluginConfigs(pkPluginConfigs)

        if (playerAlignment == FRONT_ALIGNMENT) {
            frontPlayer = KalturaOttPlayer.create(activity, playerInitOptions)
        } else {
            backgroundplayer = KalturaOttPlayer.create(activity, playerInitOptions)
        }

        if (firstPlayback) {
            Log.d(TAG,"POC Loading Front Player with OTT")
            startOttMediaLoading(frontPlayer, mediaIdOne, ks, PhoenixMediaProvider.HttpProtocol.Https, fileIdOne)
        } else {
            Log.d(TAG,"POC In Change media : playerAlignment => $playerAlignment")
            if (playerAlignment == BACKGROUND_ALIGNMENT) {
                Log.d(TAG,"POC Loading Background Player with OTT in Change Media")
                startOttMediaLoading(backgroundplayer, mediaIdTwo, ks, PhoenixMediaProvider.HttpProtocol.Https, fileIdTwo)
            } /*else {
                log.d("POC Loading Front Player with OTT in Change Media")
                startOttMediaLoading(frontPlayer, PartnersConfig.mediaId.get(0), PartnersConfig.ks, PhoenixMediaProvider.HttpProtocol.Https, PartnersConfig.fileId.get(0))
            }*/
        }
    }

    private fun startOttMediaLoading(player: KalturaPlayer? , assetId: String, ks: String?, protocol: String, fileId: String) {
        buildOttMediaOptions(player, assetId, ks, protocol, fileId)
    }

    private fun setPlayerViews(player: KalturaPlayer?) {
        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        playerLayout.addView(player?.playerView)
        addPlayerListeners(progressBar, player)
    }

    private fun buildOttMediaOptions(player: KalturaPlayer?, assetId: String, ks: String?, protocol: String, fileId: String) {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = assetId
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.urlType = APIDefines.KalturaUrlType.Direct
        ottMediaAsset.protocol = protocol //PhoenixMediaProvider.HttpProtocol.Http/s
        ottMediaAsset.ks = ks
        ottMediaAsset.mediaFileIds = listOf(fileId)

        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = 0L

        player?.loadMedia(ottMediaOptions) { entry, error ->
            if (!firstPlayback) {
                mediaEntryForBGPlayer = entry
                if (playerAlignment == FRONT_ALIGNMENT) {
                    Log.d(TAG,"POC In Playback : Front Player Playing, Background Player Paused")
                    backgroundplayer?.pause()
                    playerAlignment = BACKGROUND_ALIGNMENT
                }
            } else {
                Log.d(TAG,"POC In Very First Playback : playerAlignment => $playerAlignment")
                firstPlayback = false
                setPlayerViews(player)
                playerControls.setPlayer(player)
                startCountDown()
                loadKalturaPlayer(OTT_PARTNER_ID_POC, KalturaPlayer.Type.ott, pkPluginConfigs, BACKGROUND_ALIGNMENT)
            }

            if (error != null) {
                toast(error.message)
            } else {
                Log.d(TAG,"OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }
    }

    private fun startCountDown() {
        object : CountDownTimer(cutOffTime, 1000) {
            override fun onFinish() {
                if (playerAlignment == BACKGROUND_ALIGNMENT) {
                    Log.d(TAG, "POC Position reached destroying Front Player. Setting Background Player")
                    frontPlayer?.destroy()
                    setPlayerViews(backgroundplayer)
                    playerControls.setPlayer(backgroundplayer)
                    backgroundplayer?.isPreload = true
                    backgroundplayer?.setMedia(mediaEntryForBGPlayer!!)
                    backgroundplayer?.play()
                    firstPlayback = true
                    playerAlignment = FRONT_ALIGNMENT
                }
            }

            override fun onTick(millisUntilFinished: Long) {
                var timeLeft: Int = kotlin.math.round(((millisUntilFinished)/1000).toDouble()).toInt()
                if (timeLeft < 10) {
                    toast("Will load the next media in  $timeLeft second")
                }
            }

        }.start()
    }

    private fun addPlayerListeners(appProgressBar: ProgressBar, player: KalturaPlayer?) {
        player?.addListener(this, PlayerEvent.playheadUpdated) { event ->
            // Log.d(TAG, "playheadUpdated event  position = " + event.position + " duration = " + event.duration)
        }
    }
}