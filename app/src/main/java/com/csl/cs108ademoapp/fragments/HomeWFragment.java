package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.google.android.material.tabs.TabLayout;

public class HomeWFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager viewPager;
    MainWAdapter adapter;

    private String[] tabs = {"Normal", "Simple"};
    int iTargetOld, iSessionOld;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.custom_tabbed_layout, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        InventoryRfidiMultiFragment fragment1 = (InventoryRfidiMultiFragment) adapter.fragment1;
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
        actionBar.setTitle(R.string.title_activity_home);

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);

        MainActivity.csLibrary4A.appendToLog("adaptoer is " + (adapter == null ? "null" : "valid"));
        if (adapter != null) {
            adapter.fragment0.onDestroy();
            adapter.fragment1.onDestroy();
        }
        adapter = new MainWAdapter(getActivity().getSupportFragmentManager());
        MainActivity.csLibrary4A.appendToLog("viewPager is " + (viewPager == null ? "null" : "valid"));
        viewPager = (ViewPager) getActivity().findViewById(R.id.OperationsPager);
        viewPager.setAdapter(null); viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        for (String tab_name : tabs) {
            MainActivity.csLibrary4A.appendToLog("adding tab_name = " + tab_name);
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
        adapter.fragment0.onPause();
        adapter.fragment1.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        adapter.fragment0.onStop();
        adapter.fragment1.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        adapter.fragment0.onDestroyView();
        adapter.fragment1.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        adapter.fragment0.onDestroy();
        adapter.fragment1.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        adapter.fragment0.onDetach();
        adapter.fragment1.onDetach();
        super.onDetach();
    }

    public HomeWFragment() {
        super("HomeWFragment");
    }

    public class MainWAdapter extends FragmentStatePagerAdapter {
        private final int NO_OF_TABS = 2;
        public Fragment fragment0, fragment1;

        @Override
        public Fragment getItem(int index) {
            MainActivity.csLibrary4A.appendToLog("getItem = " + index);
            Fragment fragment = null;
            switch (index) {
                case 0:
                    fragment = new HomeFragment();
                    fragment0 = fragment;
                    break;
                case 1:
                    fragment = new DirectWedgeFragment();
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

        public MainWAdapter(FragmentManager fm) {
            super(fm);
        }
    }
}
