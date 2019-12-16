package com.kaltura.kflow.presentation.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialog
import com.kaltura.kflow.R

/**
 * Created by alex_lytvynenko on 2019-07-22.
 */
class ProgressDialog(context: Context) : AppCompatDialog(context) {
    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_progress)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}