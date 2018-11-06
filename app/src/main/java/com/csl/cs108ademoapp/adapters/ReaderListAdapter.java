package com.csl.cs108ademoapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108library4a.ReaderDevice;

import java.util.ArrayList;

public class ReaderListAdapter extends ArrayAdapter<ReaderDevice> {
    private final Context context;
    private final int resourceId;
    private final ArrayList<ReaderDevice> readersList;
    private final boolean select4detail;

    public ReaderListAdapter(Context context, int resourceId, ArrayList<ReaderDevice> readersList, boolean select4detail) {
        super(context, resourceId, readersList);
        this.context = context;
        this.resourceId = resourceId;
        this.readersList = readersList;
        this.select4detail = select4detail;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (false) MainActivity.mCs108Library4a.appendToLog("position = " + position);
        ReaderDevice reader = readersList.get(position);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(resourceId, null);
        }
        CheckedTextView checkedTextView = (CheckedTextView) convertView.findViewById(R.id.reader_checkedtextview);
        String text1 = "";
        if (reader.getName() != null) {
            if (reader.getName().length() != 0) {
                text1 += reader.getName();
            }
        }
        if (reader.getAddress() != null) {
            if (reader.getAddress().length() != 0) {
                if (text1.length() != 0) text1 += "\n";
                text1 += reader.getAddress();
            }
        }
        checkedTextView.setText(text1);
        if (reader.getSelected()) {
            checkedTextView.setChecked(true);
        } else {
            checkedTextView.setChecked(false);
        }

        TextView countTextView = (TextView) convertView.findViewById(R.id.reader_count);
        if (reader.getCount() != 0) {
            countTextView.setText(String.valueOf(reader.getCount()));
        } else {
            countTextView.setVisibility(View.GONE);
        }

        double rssiValue = reader.getRssi();
        TextView rssiTextView = (TextView) convertView.findViewById(R.id.reader_rssi);
        rssiTextView.setVisibility(View.VISIBLE);
        if (MainActivity.mCs108Library4a.getRssiDisplaySetting() != 0 && rssiValue > 0) rssiValue -= 106.98;
        rssiTextView.setText(String.format("%.1f", rssiValue));

        TextView readerDetailA = (TextView) convertView.findViewById(R.id.reader_detailA);
        TextView readerDetailB = (TextView) convertView.findViewById(R.id.reader_detailB);
        if (checkedTextView.isChecked() || select4detail == false) {
            readerDetailA.setVisibility(View.VISIBLE);
            readerDetailB.setVisibility(View.VISIBLE);
            if (reader.getDetails().length() != 0) {
                readerDetailA.setText(reader.getDetails());
            }
            readerDetailB.setText("");
            if (reader.isConnected()) {
                readerDetailB.setText("Connected");
            } else if (select4detail == false) {
                double dChannel = MainActivity.mCs108Library4a.getLogicalChannel2PhysicalFreq(reader.getChannel());
                readerDetailB.setText("Phase=" + reader.getPhase() + "\n" + dChannel + "MHz");  //"\nChannel=" + reader.getChannel());
            }
        } else {
            readerDetailA.setVisibility(View.GONE);
            readerDetailB.setVisibility(View.GONE);
        }
        return convertView;
    }
}
