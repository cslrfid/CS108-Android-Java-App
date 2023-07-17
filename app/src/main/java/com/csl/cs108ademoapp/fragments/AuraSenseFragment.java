package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

public class AuraSenseFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager viewPager;
    AuraSenseAdapter mAdapter;

    private String[] tabs = {"Configuration", "Scan" };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.custom_tabbed_layout, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        InventoryRfidiMultiFragment fragment1 = (InventoryRfidiMultiFragment) mAdapter.fragment1;
        switch (item.getItemId()) {
            case R.id.menuAction_clear:
                fragment1.clearTagsList();
                return true;
            case R.id.menuAction_sortRssi:
                fragment1.sortTagsListByRssi();
                return true;
            case R.id.menuAction_sort:
                fragment1.sortTagsList();
                return true;
            case R.id.menuAction_save:
                fragment1.saveTagsList();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_inv);
        actionBar.setTitle(R.string.title_activity_auraSense);

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);

        mAdapter = new AuraSenseAdapter(getActivity().getSupportFragmentManager());
        viewPager = (ViewPager) getActivity().findViewById(R.id.OperationsPager);
        viewPager.setAdapter(mAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        for (String tab_name : tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tab_name));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    @Override
    public void onPause() {
		mAdapter.fragment0.onPause();
        mAdapter.fragment1.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mAdapter.fragment0.onStop();
        mAdapter.fragment1.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mAdapter.fragment0.onDestroyView();
        mAdapter.fragment1.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mAdapter.fragment0.onDestroy();
        mAdapter.fragment1.onDestroy();
        MainActivity.csLibrary4A.setSelectCriteriaDisable(1);
        MainActivity.csLibrary4A.setSameCheck(true);
        MainActivity.csLibrary4A.restoreAfterTagSelect();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        mAdapter.fragment0.onDetach();
        mAdapter.fragment1.onDetach();
        super.onDetach();
    }

    public AuraSenseFragment() {
        super("AuraSenseFragment");
    }

    class AuraSenseAdapter extends FragmentStatePagerAdapter {
        private final int NO_OF_TABS = 2;
        public Fragment fragment0, fragment1, fragment2;

        @Override
        public Fragment getItem(int index) {
            Fragment fragment = null;
            switch (index) {
                case 0:
                    fragment = new AccessAuraSenseFragment();
                    fragment0 = fragment;
                    break;
                default:
                    fragment = InventoryRfidiMultiFragment.newInstance(true,"E280B12");
                    fragment1 = fragment;
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

        public AuraSenseAdapter(FragmentManager fm) {
            super(fm);
        }
    }
}
