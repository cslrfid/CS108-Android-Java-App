package com.csl.cslibrary4a;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

public class AdapterTab extends FragmentStatePagerAdapter {
    private int iNumOfTabs;
    public Fragment fragment0, fragment1, fragment2, fragment3, fragment4 = null;

    public AdapterTab(FragmentManager fm, int iNumOfTabs) {
        super(fm);
        this.iNumOfTabs = iNumOfTabs;
    }

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;
        switch (index) {
            case 0:
                fragment = fragment0;
                break;
            case 1:
                fragment = fragment1;
                break;
            case 2:
                fragment = fragment2;
                break;
            case 3:
                fragment = fragment3;
                break;
            case 4:
                fragment = fragment4;
                break;
            default:
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return iNumOfTabs;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    public void setFragment(int index, Fragment fragment) {
        switch (index) {
            case 0:
                fragment0 = fragment;
                break;
            case 1:
                fragment1 = fragment;
                break;
            case 2:
                fragment2 = fragment;
                break;
            case 3:
                fragment3 = fragment;
                break;
            case 4:
                fragment4 = fragment;
                break;
            default:
                break;
        }
    }
}
