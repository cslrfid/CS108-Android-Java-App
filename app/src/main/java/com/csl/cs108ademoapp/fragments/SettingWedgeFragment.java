package com.csl.cs108ademoapp.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.DialogFragment;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

import java.io.File;
import java.io.FileOutputStream;

public class SettingWedgeFragment extends DialogFragment {
    EditText editTextPower, editTextPrefix, editTextSuffix;
    Spinner spinnerDelimiter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_directwedge_settings, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Simple Wedge Settings");
        //builder.setMessage("simple wedge setttings");
        builder.setView(view);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String wedgePower = editTextPower.getText().toString();
                try {
                    int iWedgePower = Integer.parseInt(wedgePower);
                    if (iWedgePower < 0) iWedgePower = 0;
                    else if (iWedgePower > 300) iWedgePower = 300;
                    editTextPower.setText(String.valueOf(iWedgePower));
                    MainActivity.wedgePower = iWedgePower;
                } catch (Exception ex) { }
                MainActivity.wedgePrefix = editTextPrefix.getText().toString();
                MainActivity.wedgeSuffix = editTextSuffix.getText().toString();
                int wedgeDelimiter = 0x0A;
                switch (spinnerDelimiter.getSelectedItemPosition()) {
                    default:
                        break;
                    case 1:
                        wedgeDelimiter = 0x09;
                        break;
                    case 2:
                        wedgeDelimiter = 0x2c;
                        break;
                    case 3:
                        wedgeDelimiter = 0x20;
                        break;
                    case 4:
                        wedgeDelimiter = -1;
                        break;
                }
                MainActivity.wedgeDelimiter = wedgeDelimiter;
                saveWedgeSetting2File();
                getDialog().dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                getDialog().dismiss();
            }
        });

        editTextPower = (EditText) view.findViewById(R.id.directWedgeSettingEditTextPower);
        MainActivity.csLibrary4A.appendToLog("editTextPower is " + (editTextPower == null ? "null" : "valid"));
        if (editTextPower != null) editTextPower.setText(String.valueOf(MainActivity.wedgePower));

        editTextPrefix = (EditText) view.findViewById(R.id.directWedgeSettingEditTextPrefix);
        MainActivity.csLibrary4A.appendToLog("editTextPrefix is " + (editTextPrefix == null ? "null" : "valid"));
        if (editTextPrefix != null) editTextPrefix.setText(MainActivity.wedgePrefix);

        editTextSuffix = (EditText) view.findViewById(R.id.directWedgeSettingEditTextSuffix);
        MainActivity.csLibrary4A.appendToLog("editTextSuffix is " + (editTextSuffix == null ? "null" : "valid"));
        if (editTextSuffix != null) editTextSuffix.setText(MainActivity.wedgeSuffix);

        spinnerDelimiter = (Spinner) view.findViewById(R.id.directWedgeSettingSpinnerDelimiter);
        MainActivity.csLibrary4A.appendToLog("spinnerDelimiter is " + (spinnerDelimiter == null ? "null" : "valid"));
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.delimiter_options, R.layout.custom_spinner_layout);
        MainActivity.csLibrary4A.appendToLog("targetAdapter is " + (targetAdapter == null ? "null" : "valid"));
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int position = 0;
        switch (MainActivity.wedgeDelimiter) {
            default:
                position = 0;
                break;
            case 0x09:
                position = 1;
                break;
            case 0x2C:
                position = 2;
                break;
            case 0x20:
                position = 3;
                break;
            case -1:
                position = 4;
                break;
        }
        MainActivity.csLibrary4A.appendToLog("position is " + position);
        spinnerDelimiter.setAdapter(targetAdapter);
        spinnerDelimiter.setSelection(position);
        return builder.create();
    }

    void saveWedgeSetting2File() {
        File path = getContext().getFilesDir();
        File file = new File(path, MainActivity.fileName);
        FileOutputStream stream;
        try {
            stream = new FileOutputStream(file);
            write2FileStream(stream, "Start of data\n");
            write2FileStream(stream, "wedgePower," + MainActivity.wedgePower + "\n");
            write2FileStream(stream, "wedgePrefix," + MainActivity.wedgePrefix + "\n");
            write2FileStream(stream, "wedgeSuffix," + MainActivity.wedgeSuffix + "\n");
            write2FileStream(stream, "wedgeDelimiter," + String.valueOf(MainActivity.wedgeDelimiter) + "\n");
            write2FileStream(stream, "End of data\n");
            stream.close();
        } catch (Exception ex){
            //
        }
    }
    void write2FileStream(FileOutputStream stream, String string) {
        boolean DEBUG = true;
        try {
            stream.write(string.getBytes()); if (DEBUG) MainActivity.csLibrary4A.appendToLog("outData = " + string);
        } catch (Exception ex) { }
    }
}
