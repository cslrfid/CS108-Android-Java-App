package com.csl.cs108ademoapp.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.DialogFragment;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

public class SettingWedgeFragment extends DialogFragment {
    EditText editTextPower, editTextPrefix, editTextSuffix;
    Spinner spinnerDelimiter, spinnerOutput;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_directwedge_settings, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Wedge Settings");
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
                    MainActivity.csLibrary4A.setWedgePower(iWedgePower);
                } catch (Exception ex) { }
                MainActivity.csLibrary4A.setWedgePrefix(editTextPrefix.getText().toString());
                MainActivity.csLibrary4A.setWedgeSuffix(editTextSuffix.getText().toString());
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
                MainActivity.csLibrary4A.setWedgeDelimiter(wedgeDelimiter);
                MainActivity.csLibrary4A.appendToLog("SettingWedgeFragment, onCreateDialog: wedgeDelimiter = " + MainActivity.csLibrary4A.getWedgeOutput());
                MainActivity.csLibrary4A.setWedgeOutput(spinnerOutput.getSelectedItemPosition());
                MainActivity.csLibrary4A.saveWedgeSetting2File();
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
        if (editTextPower != null) editTextPower.setText(String.valueOf(MainActivity.csLibrary4A.getWedgePower()));

        editTextPrefix = (EditText) view.findViewById(R.id.directWedgeSettingEditTextPrefix);
        MainActivity.csLibrary4A.appendToLog("editTextPrefix is " + (editTextPrefix == null ? "null" : "valid"));
        if (editTextPrefix != null) editTextPrefix.setText(MainActivity.csLibrary4A.getWedgePrefix());

        editTextSuffix = (EditText) view.findViewById(R.id.directWedgeSettingEditTextSuffix);
        MainActivity.csLibrary4A.appendToLog("editTextSuffix is " + (editTextSuffix == null ? "null" : "valid"));
        if (editTextSuffix != null) editTextSuffix.setText(MainActivity.csLibrary4A.getWedgeSuffix());

        spinnerDelimiter = (Spinner) view.findViewById(R.id.directWedgeSettingSpinnerDelimiter);
        MainActivity.csLibrary4A.appendToLog("spinnerDelimiter is " + (spinnerDelimiter == null ? "null" : "valid"));
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(getActivity(), R.array.delimiter_options, R.layout.custom_spinner_layout);
        MainActivity.csLibrary4A.appendToLog("targetAdapter is " + (targetAdapter == null ? "null" : "valid"));
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int position = 0;
        MainActivity.csLibrary4A.appendToLog("SettingWedgeFragment, onCreatDialog: wedgeDelimiter = " + MainActivity.csLibrary4A.getWedgeDelimiter());
        switch (MainActivity.csLibrary4A.getWedgeDelimiter()) {
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

        spinnerOutput = (Spinner) view.findViewById(R.id.directWedgeSettingSpinnerOutput);
        MainActivity.csLibrary4A.appendToLog("spinnerOutput is " + (spinnerOutput == null ? "null" : "valid"));
        ArrayAdapter<CharSequence> targetAdapter1 = ArrayAdapter.createFromResource(getActivity(), R.array.wedgeOutput_options, R.layout.custom_spinner_layout);
        MainActivity.csLibrary4A.appendToLog("targetAdapter1 is " + (targetAdapter1 == null ? "null" : "valid"));
        targetAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        int position1 = MainActivity.csLibrary4A.getWedgeOutput();
        MainActivity.csLibrary4A.appendToLog("position1 is " + position1);
        spinnerOutput.setAdapter(targetAdapter1);
        spinnerOutput.setSelection(position1);

        return builder.create();
    }
}
