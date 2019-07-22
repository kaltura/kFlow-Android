package com.kaltura.kflow.presentation.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDialog;

import com.kaltura.kflow.R;

/**
 * Created by alex_lytvynenko on 2019-07-22.
 */
public class ProgressDialog extends AppCompatDialog {

    public ProgressDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }
}