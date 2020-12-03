package com.csl.cs108ademoapp.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.csl.cs108ademoapp.fragments.SettingFilterPostFragment;
import com.csl.cs108ademoapp.fragments.SettingFilterPreFragment;

public class FilterAdapter extends FragmentStatePagerAdapter {
    private final int NO_OF_TABS = 2;
    public Fragment fragment0, fragment1;

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new SettingFilterPreFragment();
                fragment0 = fragment;
                break;
            case 1:
                fragment = new SettingFilterPostFragment();
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

    public FilterAdapter(FragmentManager fm) {
        super(fm);
    }
}
