package com.kaltura.kflow.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kaltura.kflow.R

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val STORAGE_PERMISSIONS_REQUEST_CODE = 123

    // Turn this true to start logcat logging
    val enableLogsCapturing = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (enableLogsCapturing) {
            getPermissionToStorage()
        }
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