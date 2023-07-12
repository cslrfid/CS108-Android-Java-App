package com.csl.cs108ademoapp;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.csl.cs108ademoapp.MainActivity.csLibrary4A;

public class CustomPopupWindow {
    Context context;
    public CustomPopupWindow(Context context) {
        this.context = context;
    }

    public PopupWindow popupWindow;
    public void popupStart(String message, boolean wait) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        TextView textViewDismiss = (TextView)popupView.findViewById(R.id.dismissMessage);
        int iLenghtMax = 300;
        if (message.length() > iLenghtMax) message = message.substring(0, iLenghtMax) + " .....";
        csLibrary4A.appendToLog("SaveList2ExternalTask: popupStart message = " + message);
        textViewDismiss.setText(message);
        Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);
        if (wait) btnDismiss.setVisibility(View.GONE);
        else {
            btnDismiss.setOnClickListener(new Button.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
        }
    }
}
