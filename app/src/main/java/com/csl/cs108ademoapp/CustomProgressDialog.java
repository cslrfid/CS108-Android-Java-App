package com.csl.cs108ademoapp;

import android.app.ProgressDialog;
import android.content.Context;

public class CustomProgressDialog extends ProgressDialog {
    public CustomProgressDialog(Context context, String message) {
        super(context, ProgressDialog.STYLE_SPINNER);
        if (message == null) message = "Progressing. Please wait.";
        setTitle(null);
        setMessage(message);
        setCancelable(false);
    }
}
