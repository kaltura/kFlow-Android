package com.kaltura.kflow.presentation.extension

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import java.io.File

/**
 * Created by alex_litvinenko
 */
fun FragmentActivity.shareFile(file: File) {
    if (file.exists()) {
        val intentShareFile = Intent(Intent.ACTION_SEND)
        intentShareFile.type = "text/plain"
        val fileUri: Uri = if (Build.VERSION.SDK_INT > 21) {
            FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
        intentShareFile.putExtra(Intent.EXTRA_STREAM, fileUri)
        intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing request data")
        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing request data...")
        startActivity(Intent.createChooser(intentShareFile, "Share request data"))
    }
}