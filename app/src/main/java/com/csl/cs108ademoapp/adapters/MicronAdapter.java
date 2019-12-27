package com.csl.cs108ademoapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.csl.cs108ademoapp.fragments.AccessMicronFragment;
import com.csl.cs108ademoapp.fragments.AccessUcodeFragment;
import com.csl.cs108ademoapp.fragments.InventoryRfidiMultiFragment;

public class MicronAdapter extends FragmentStatePagerAdapter {
    private final int NO_OF_TABS = 3;
    public Fragment fragment0, fragment1, fragment2;

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;
        switch (index) {
            case 0:
                fragment = AccessMicronFragment.newInstance(false);
                fragment0 = fragment;
                break;
            case 1:
                fragment = InventoryRfidiMultiFragment.newInstance(true, "", false);
                fragment1 = fragment;
                break;
            case 2:
                fragment = new AccessUcodeFragment();
                fragment2 = fragment;
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

    public MicronAdapter(FragmentManager fm) {
        super(fm);
    }
}
