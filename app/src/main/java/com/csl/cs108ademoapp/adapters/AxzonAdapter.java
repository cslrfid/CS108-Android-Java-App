package com.csl.cs108ademoapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.csl.cs108ademoapp.fragments.AccessMicronFragment;
import com.csl.cs108ademoapp.fragments.AccessUcodeFragment;
import com.csl.cs108ademoapp.fragments.AccessXerxesLoggerFragment;
import com.csl.cs108ademoapp.fragments.InventoryRfidiMultiFragment;

public class AxzonAdapter extends FragmentStatePagerAdapter {
    public int NO_OF_TABS = 4;
    public Fragment fragment0, fragment1, fragment2, fragment3;

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;
        switch (index) {
            case 0:
                fragment = InventoryRfidiMultiFragment.newInstance(true, "");
                fragment0 = fragment;
                break;
            case 1:
                fragment = AccessMicronFragment.newInstance(true);
                fragment1 = fragment;
                break;
            case 2:
                fragment = new AccessXerxesLoggerFragment();
                fragment2 = fragment;
                break;
            case 3:
                fragment = new AccessUcodeFragment();
                fragment3 = fragment;
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

    public static AxzonAdapter newinstance(FragmentManager fm, int tabsCount) {
        AxzonAdapter adapter = new AxzonAdapter(fm);
        adapter.NO_OF_TABS = tabsCount;
        return adapter;
    }
    public AxzonAdapter(FragmentManager fm) {
        super(fm);
    }
}
