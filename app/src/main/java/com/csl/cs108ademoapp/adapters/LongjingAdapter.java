package com.csl.cs108ademoapp.adapters;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.csl.cs108ademoapp.fragments.AccessKilowayFragment;
import com.csl.cs108ademoapp.fragments.InventoryRfidSearchFragment;
import com.csl.cs108ademoapp.fragments.InventoryRfidiMultiFragment;

public class LongjingAdapter extends FragmentStatePagerAdapter {
    private final int NO_OF_TABS = 2;
    public Fragment fragment0, fragment1;

    @Override
    public Fragment getItem(int index) {
        Fragment fragment = null;
        switch (index) {
            case 0:
                fragment = InventoryRfidiMultiFragment.newInstance(true,"E201E");
                fragment1 = fragment;
                break;
            case 2:
                fragment = new AccessKilowayFragment(true);
                fragment0 = fragment;
                break;
            case 1:
                fragment = new InventoryRfidSearchFragment(true);
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

    public LongjingAdapter(FragmentManager fm) {
        super(fm);
    }
}
