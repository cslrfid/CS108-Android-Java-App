package com.csl.cs108ademoapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.csl.cs108ademoapp.DrawerListContent;
import com.csl.cs108ademoapp.R;

import java.util.List;

/**
 * Class to handle the data for NavigationDrawer.
 */
public class DrawerListAdapter extends ArrayAdapter {
    Context mContext;
    List<DrawerListContent.DrawerItem> mData = null;

    /**
     * Construtor. Handles the initialization.
     *
     * @param context  - context to be used
     * @param resource - layout to be inflated
     * @param objects  - navidation drawer items
     */
    public DrawerListAdapter(Context context, int resource, List<DrawerListContent.DrawerItem> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mData = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.drawer_list_item, parent, false);
        }

        DrawerListContent.DrawerItem item = mData.get(position);
        //Set the label
        TextView label1 = (TextView) convertView.findViewById(R.id.drawerItemName);
        label1.setText(item.content);
        return convertView;
    }
}
