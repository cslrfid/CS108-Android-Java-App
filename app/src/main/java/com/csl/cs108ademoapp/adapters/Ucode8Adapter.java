package com.csl.cs108ademoapp.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.csl.cs108ademoapp.fragments.AccessUcode8Fragment;
import com.csl.cs108ademoapp.fragments.InventoryRfidiMultiFragment;
import com.csl.cs108ademoapp.fragments.UtraceFragment;

public class Ucode8Adapter extends FragmentStatePagerAdapter {
    private final int NO_OF_TABS = 2;
    public Fragment fragment0, fragment1, fragment2;

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;
        switch (index) {
            case 0:
                fragment = new AccessUcode8Fragment();
                fragment0 = fragment;
                break;
            case 1:
                fragment = InventoryRfidiMultiFragment.newInstance(true,"E2806894");
                fragment1 = fragment;
                break;
            case 2:
                fragment = new UtraceFragment();
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

    public Ucode8Adapter(FragmentManager fm) {
        super(fm);
    }
}
