package com.csl.cs108ademoapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;

import com.csl.cs108ademoapp.fragments.InventoryBarcodeFragment;
import com.csl.cs108ademoapp.fragments.InventoryRfidiMultiFragment;

public class InventoryAdapter extends FragmentStatePagerAdapter {
    private final int NO_OF_TABS = 2;
    public Fragment fragment0, fragment1;

    @Override
    public Fragment getItem(int index) {
        Log.i("Hello", "InventoryAdadpter.getItem");
        Fragment fragment = null;
        switch (index) {
            case 0:
                fragment = InventoryRfidiMultiFragment.newInstance(false, null, false);
                fragment0 = fragment;
                break;
            case 1:
                fragment = new InventoryBarcodeFragment();
                fragment1 = fragment;
                break;
            default:
                fragment = null;
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return NO_OF_TABS;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    public InventoryAdapter(FragmentManager fm) {
        super(fm);
    }
}
