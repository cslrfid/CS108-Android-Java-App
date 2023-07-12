package com.csl.cs108ademoapp;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableRow;

import static com.csl.cs108ademoapp.MainActivity.tagSelected;

public class SelectTag {
    public EditText editTextTagID, editTextSelectOffset, editTextAccessPassword, editTextAccessAntennaPower;
    public Spinner spinnerSelectBank;
    public TableRow tableRowSelectMemoryBank, tableRowSelectPassword;

    public SelectTag(Activity activity) {
        tableRowSelectMemoryBank = (TableRow) activity.findViewById(R.id.selectMemoryBankRow);
        tableRowSelectPassword = (TableRow) activity.findViewById(R.id.selectPasswordRow);

        editTextTagID = (EditText) activity.findViewById(R.id.selectTagID);
        editTextSelectOffset = (EditText) activity.findViewById(R.id.selectMemoryOffset);
        //editTextRWSelectOffset.setVisibility(View.VISIBLE);

        spinnerSelectBank = (Spinner) activity.findViewById(R.id.selectMemoryBank);
        ArrayAdapter<CharSequence> targetAdapter = ArrayAdapter.createFromResource(activity, R.array.read_memoryBank_options, R.layout.custom_spinner_layout);
        targetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelectBank.setAdapter(targetAdapter);
        spinnerSelectBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setBankSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        editTextAccessPassword = (EditText) activity.findViewById(R.id.selectPasswordValue);
        editTextAccessPassword.addTextChangedListener(new GenericTextWatcher(editTextAccessPassword, 8));
        editTextAccessPassword.setText("00000000");

        editTextAccessAntennaPower = (EditText) activity.findViewById(R.id.selectAntennaPower);
        editTextAccessAntennaPower.setText(String.valueOf(300));
    }

    public void updateBankSelected() {
        setBankSelected(-1);
    }
    void setBankSelected(int position) {
        if (position < 0 || position > 2) position = spinnerSelectBank.getSelectedItemPosition();
        switch (position) {
            case 0: //if EPC
                if (tagSelected != null) editTextTagID.setText(tagSelected.getAddress());
                editTextSelectOffset.setText("32");
                break;
            case 1:
                if (tagSelected != null) { if (tagSelected.getTid() != null) editTextTagID.setText(tagSelected.getTid()); }
                editTextSelectOffset.setText("0");
                break;
            case 2:
                if (tagSelected != null) { if (tagSelected.getUser() != null) editTextTagID.setText(tagSelected.getUser()); }
                editTextSelectOffset.setText("0");
                break;
            default:
                break;
        }
    }
}
