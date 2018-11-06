package com.csl.cs108ademoapp.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.fragments.SettingAdminFragment;
import com.csl.cs108ademoapp.fragments.SettingOperateFragment;

public class SettingAdapter extends FragmentStatePagerAdapter {
    private int mNumOfTabs;
    public Fragment fragment0, fragment1, fragment2, fragment3, fragment4 = null;

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;
        switch (index) {
            case 0:
                fragment = new SettingOperateFragment();
                fragment0 = fragment;
                break;
            case 1:
                fragment = new SettingAdminFragment();
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
        return mNumOfTabs;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    public SettingAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }
}
