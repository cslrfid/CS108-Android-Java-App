package com.csl.cs108ademoapp.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.csl.cs108ademoapp.fragments.AccessColdChainFragment;
import com.csl.cs108ademoapp.fragments.AccessEm4325PassiveFragment;
import com.csl.cs108ademoapp.fragments.InventoryRfidiMultiFragment;

public class ColdChainAdapter extends FragmentStatePagerAdapter {
    private final int NO_OF_TABS = 3;
    public Fragment fragment0, fragment1, fragment2;

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;
        switch (index) {
            case 2:
                fragment = new AccessEm4325PassiveFragment();
                fragment1 = fragment;
                break;
            case 1:
                fragment = new AccessColdChainFragment();
                fragment2 = fragment;
                break;
            case 0:
                fragment = InventoryRfidiMultiFragment.newInstance(true,"E280B0");
                fragment0 = fragment;
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

    public ColdChainAdapter(FragmentManager fm) {
        super(fm);
    }
}
