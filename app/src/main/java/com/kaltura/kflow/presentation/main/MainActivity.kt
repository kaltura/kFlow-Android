package com.kaltura.kflow.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.kaltura.kflow.R
import com.kaltura.kflow.presentation.settings.SettingsViewModel
import com.kaltura.playkit.player.MediaSupport
import com.kaltura.tvplayer.KalturaOttPlayer
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val STORAGE_PERMISSIONS_REQUEST_CODE = 123
    private val PUSH_NOTIFICATION_REQUEST_CODE = 256

    // Turn this true to start logcat logging
    val enableLogsCapturing = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (enableLogsCapturing) {
            getPermissionToStorage()
        }
        askNotificationPermission()
    }

    private fun getPermissionToStorage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            + ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(
                    "Read External Storage and Write External Storage" +
                            " Storage permissions are required to do the task."
                )
                builder.setTitle("Please grant those permissions")
                builder.setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this@MainActivity, arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        STORAGE_PERMISSIONS_REQUEST_CODE
                    )
                }
                builder.setNeutralButton("Cancel", null)
                val dialog: AlertDialog = builder.create()
                dialog.show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    STORAGE_PERMISSIONS_REQUEST_CODE
                )
            }
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
                val builder = AlertDialog.Builder(this)
                builder.setMessage(
                    "Post Notification Permission" +
                            " required for FCM."
                )
                builder.setTitle("Please grant those permissions")
                builder.setPositiveButton("OK") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this@MainActivity, arrayOf(
                            Manifest.permission.POST_NOTIFICATIONS
                        ),
                        PUSH_NOTIFICATION_REQUEST_CODE
                    )
                }
                builder.setNeutralButton("Cancel", null)
                val dialog: AlertDialog = builder.create()
                dialog.show()
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            promptMessage("Push notification will be enabled once registered to SNS")
        } else {
            promptMessage("Push notification will no be received")
        }
    }

    // Callback with the request from calling getPermissionToStorage(...)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == STORAGE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                promptMessage("Permissions granted.")
            } else {
                promptMessage("Permissions denied.")
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun promptMessage(title: String) {
        Toast.makeText(this, title, Toast.LENGTH_SHORT).show()
    }
}