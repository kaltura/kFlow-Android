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
import com.kaltura.playkit.Player
import com.kaltura.playkit.PlayerState
import kotlinx.android.synthetic.main.view_player_control.view.*
import java.text.SimpleDateFormat
import java.util.*

class PlaybackControlsView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    private val log = PKLog.get("PlaybackControlsView")
    private val PROGRESS_BAR_MAX = 100

    var player: Player? = null
    var asset: Asset? = null
    private var playerState: PlayerState = PlayerState.IDLE
    private val formatBuilder = StringBuilder()
    private val formatter = Formatter(formatBuilder, Locale.getDefault())
    private var dragging = false
    private val updateProgressAction = Runnable { updateProgress() }

    init {
        inflate(R.layout.view_player_control, true)
        play.setOnClickListener { player?.play() }
        pause.setOnClickListener { player?.pause() }
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
        if (asset == null) updateVodProgress() else updateLiveProgress()
    }

    private fun updateLiveProgress() {
        val startDate = asset?.startDate ?: 0L
        val endDate = asset?.endDate ?: 0L
        val durationMs = endDate - startDate
        val currentProgress = Date().time / 1000 - startDate
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val startTime = dateFormat.format(Date(startDate))
        val endTime = dateFormat.format(Date(endDate))
        timeCurrent.text = startTime
        time.text = endTime
        mediacontrollerProgress.progress = (PROGRESS_BAR_MAX.toFloat() * currentProgress / durationMs).toInt()
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
        return if (hours > 0) formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString() else formatter.format("%02d:%02d", minutes, seconds).toString()
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