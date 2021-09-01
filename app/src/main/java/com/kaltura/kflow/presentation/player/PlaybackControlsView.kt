package com.kaltura.kflow.presentation.player

import android.content.Context
import android.util.AttributeSet
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import com.kaltura.android.exoplayer2.C
import com.kaltura.client.types.Asset
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.extension.getColor
import com.kaltura.kflow.presentation.extension.inflate
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerState
import com.kaltura.tvplayer.KalturaPlayer
import kotlinx.android.synthetic.main.view_player_control.view.*
import java.text.SimpleDateFormat
import java.util.*
import android.provider.Settings.System.DATE_FORMAT
import android.util.Log
import java.text.DateFormat


class PlaybackControlsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val log = PKLog.get("PlaybackControlsView")
    private val PROGRESS_BAR_MAX = 100

    var player: KalturaPlayer? = null
    var asset: Asset? = null
    var isLive = false
    var onPlayerLiveSeek: (positionMs: Long) -> Unit = {}
    private var pausedPosition = 0L
    private var playerState: PlayerState = PlayerState.IDLE
    private val formatBuilder = StringBuilder()
    private val formatter = Formatter(formatBuilder, Locale.getDefault())
    private var dragging = false
    private val updateProgressAction = Runnable { updateProgress() }

    init {
        inflate(R.layout.view_player_control, true)
        play.setOnClickListener {
            if (isLive) {
                onPlayerLiveSeek(pausedPosition)
            } else {
                player?.play()
            }
        }
        pause.setOnClickListener {
            if (isLive) {
                pausedPosition =
                    Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / 1000 - (asset?.startDate
                        ?: 0L)
                Log.e("pause", "pausedPosition = $pausedPosition")
            }
            player?.pause()
        }
        mediacontrollerProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    timeCurrent.text = stringForTime(positionValue(progress))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                dragging = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                dragging = false
                player?.seekTo(positionValue(seekBar.progress))
            }

        })
    }

    private fun updateProgress() {
        if (isLive) updateLiveProgress() else updateVodProgress()
    }

    private fun updateLiveProgress() {
        val startDate = asset?.startDate ?: 0L
        val endDate = asset?.endDate ?: 0L
        val durationMs = endDate - startDate

        val startDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        startDayCalendar.timeInMillis = startDate * 1000
        val endDayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        endDayCalendar.timeInMillis = endDate * 1000

        val currentProgress =
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis / 1000 - startDate
        val dateFormat = SimpleDateFormat("HH:mm", Locale.US)
        val startTime = dateFormat.format(startDayCalendar.time)
        val endTime = dateFormat.format(endDayCalendar.time)
        timeCurrent.text = startTime
        time.text = endTime
        mediacontrollerProgress.progress = (PROGRESS_BAR_MAX * currentProgress / durationMs).toInt()
    }

    private fun updateVodProgress() {
        val duration = player?.duration ?: C.TIME_UNSET
        val position = player?.currentPosition ?: C.POSITION_UNSET.toLong()
        val bufferedPosition = player?.bufferedPosition ?: 0L

        if (duration != C.TIME_UNSET) {
            log.d("updateProgress Set Duration:$duration")
            time.text = stringForTime(duration)
        }
        if (!dragging && position != C.POSITION_UNSET.toLong() && duration != C.TIME_UNSET) {
            log.d("updateProgress Set Position:$position")
            timeCurrent.text = stringForTime(position)
            mediacontrollerProgress.progress = progressBarValue(position)
        }
        mediacontrollerProgress.secondaryProgress = progressBarValue(bufferedPosition)
        // Remove scheduled updates.
        removeCallbacks(updateProgressAction)
        // Schedule an update if necessary.
        if (playerState != PlayerState.IDLE) {
            postDelayed(updateProgressAction, 1000L)
        }
    }

    private fun progressBarValue(position: Long): Int {
        var progressValue = 0
        if (player != null) {
            val duration = player!!.duration
            if (duration > 0) {
                progressValue = (position * PROGRESS_BAR_MAX / duration).toInt()
            }
        }
        return progressValue
    }

    private fun positionValue(progress: Int): Long {
        var positionValue: Long = 0
        if (player != null) {
            val duration = player!!.duration
            positionValue = duration * progress / PROGRESS_BAR_MAX
        }
        return positionValue
    }

    private fun stringForTime(timeMs: Long): String {
        val totalSeconds = (timeMs + 500) / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        formatBuilder.setLength(0)
        return if (hours > 0) formatter.format("%d:%02d:%02d", hours, minutes, seconds)
            .toString() else formatter.format("%02d:%02d", minutes, seconds).toString()
    }

    fun setPlayerState(playerState: PlayerState) {
        this.playerState = playerState
        updateProgress()
    }

    fun setOnStartOverClickListener(listener: OnClickListener?) {
        startover.setOnClickListener(listener)
    }

    fun release() = removeCallbacks(updateProgressAction)

    fun resume() = updateProgress()

    fun disableControllersForLive() {
        pause.isEnabled = false
        play.isEnabled = false
        pause.drawable.mutate().setTint(getColor(android.R.color.darker_gray))
        play.drawable.mutate().setTint(getColor(android.R.color.darker_gray))
        mediacontrollerProgress.setOnTouchListener { _, _ -> true }
    }
}