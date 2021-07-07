package com.kaltura.kflow.presentation.base

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.annotation.LayoutRes
import androidx.core.content.FileProvider
import com.kaltura.kflow.BuildConfig
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.debug.DebugFragment
import com.kaltura.kflow.presentation.extension.toast
import com.kaltura.kflow.presentation.main.Feature
import com.kaltura.kflow.presentation.main.MainActivity
import com.kaltura.kflow.presentation.ui.SharedTransition
import com.kaltura.playkit.PKLog
import kotlinx.android.synthetic.main.view_shared_transition_header.*
import kotlinx.android.synthetic.main.view_shared_transition_header.view.*
import java.io.File

/**
 * Created by alex_lytvynenko on 05.03.2020.
 */
abstract class SharedTransitionFragment(@LayoutRes contentLayoutId: Int) :
    DebugFragment(contentLayoutId) {

    private var isFirstEnter = true
    abstract val feature: Feature

    private val logFileName = "KFlowLogs.txt"
    private var logsCapturingProcess: Process? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = SharedTransition()
        sharedElementReturnTransition = SharedTransition()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (header == null)
            throw IllegalStateException("Fragment must include R.layout.view_shared_transition_header view")

        header.sharedTransitionTitle.transitionName = "${feature.text}_title"
        header.sharedTransitionImage.transitionName = "${feature.text}_image"
        header.sharedTransitionTitle.text = feature.text
        header.sharedTransitionImage.setImageResource(feature.imageResId)
        animateCardEnter()
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        if ((requireActivity() as MainActivity).enableLogsCapturing) {
            toolbar.inflateMenu(R.menu.menu_main)
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.start_logging -> {
                        if (logsCapturingProcess == null) appendLogsToFile()
                        else toast("Logs are already being captured")
                        true
                    }
                    R.id.send_email -> {
                        sendLogsToEmail()
                        true
                    }
                    R.id.stop_logging -> {
                        if (logsCapturingProcess != null) stopLogging()
                        else toast("No log capture process found.")
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun animateCardEnter() {
        if (isFirstEnter) {
            isFirstEnter = false
            val animation = ScaleAnimation(
                0f, 1f, 0f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 500
                interpolator = AccelerateDecelerateInterpolator()
            }
            header.sharedTransitionCard.animation = animation
            animation.start()
        }
    }

    private fun appendLogsToFile() {
        PKLog.setGlobalLevel(PKLog.Level.verbose)
        val filePath = File(requireContext().filesDir, "logs")
        val file = File(filePath, logFileName)
        if (filePath.exists().not()) filePath.mkdirs()
        file.deleteOnExit()
        Runtime.getRuntime().exec("logcat -b all -c")
        val cmd = "logcat -f" + file.absolutePath
        logsCapturingProcess = Runtime.getRuntime().exec(cmd)
        toast("Logging Started..")
    }

    private fun stopLogging() {
        PKLog.setGlobalLevel(PKLog.Level.debug)
        if (logsCapturingProcess != null) {
            logsCapturingProcess!!.destroy()
            logsCapturingProcess = null
            toast("Logging Stopped..")
        }
    }

    private fun sendLogsToEmail() {
        if (logsCapturingProcess != null) {
            toast("Please stop the logging.")
            return
        }
        try {
            val emailIntent = Intent()
            emailIntent.action = Intent.ACTION_SEND
            emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            emailIntent.type = "vnd.android.cursor.dir/email"
            emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("your_email_address@gmail.com"))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sending Playkit Logs")

            val filePath = File(requireContext().filesDir, "logs")
            val file = File(filePath, logFileName)
            if (file.exists()) {
                val fileURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    file
                )
                emailIntent.putExtra(Intent.EXTRA_STREAM, fileURI)
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Sending playkit logs")
                this.startActivity(Intent.createChooser(emailIntent, "Sending email..."))
            } else {
                toast("There is no log file. Please start the session to capture logs.")
            }
        } catch (t: Throwable) {
            toast("Request failed try again: $t")
        }
    }
}