package com.csl.cslibrary4a;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CustomTabAdapter extends FragmentStateAdapter {
    private int iNumOfTabs;
    public Fragment fragment0, fragment1, fragment2, fragment3, fragment4 = null;

    public CustomTabAdapter(Fragment fragment, int iNumOfTabs) {
        super(fragment);
        this.iNumOfTabs = iNumOfTabs;
    }

    @Override
    public Fragment createFragment(int index) {
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
    public int getItemCount() {
        return iNumOfTabs;
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
