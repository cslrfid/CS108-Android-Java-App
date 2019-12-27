package com.csl.cs108ademoapp;

import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.widget.EditText;

public class GenericTextWatcher implements TextWatcher {
    EditText editText;
    int maxLength;
    InputFilter mInputFilter;

    public GenericTextWatcher(EditText editText, int maxLength) {
        this.editText = editText;
        this.maxLength = maxLength;
        mInputFilter = new InputFilter.LengthFilter(maxLength);
        editText.setFilters(new InputFilter[] { mInputFilter });
    }

    public void afterTextChanged(Editable s) {
        if (s.length() != 0 && s.length() < maxLength) {
            editText.setTextColor(Color.RED);
        } else
            editText.setTextColor(Color.BLACK);
    }
    public void beforeTextChanged(CharSequence s, int start, int count, int after){}
    public void onTextChanged(CharSequence s, int start, int before, int count){}
}