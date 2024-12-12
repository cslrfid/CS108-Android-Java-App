package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.adapters.MyAdapter;
import com.google.android.material.tabs.TabLayout;

public class HomeWFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager viewPager;
    MyAdapter adapter;

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
        if (item.getItemId() == R.id.menuAction_clear) {
            fragment1.clearTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_sortRssi) {
            fragment1.sortTagsListByRssi();
            return true;
        } else if (item.getItemId() == R.id.menuAction_sort) {
            fragment1.sortTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_save) {
            fragment1.saveTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_share) {
            fragment1.shareTagsList();
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_inv);
        actionBar.setTitle(R.string.title_activity_home_cs108);

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);

        MainActivity.csLibrary4A.appendToLog("adaptoer is " + (adapter == null ? "null" : "valid"));
        if (adapter != null) {
            adapter.fragment0.onDestroy();
            adapter.fragment1.onDestroy();
        }

        adapter = new MyAdapter(getActivity().getSupportFragmentManager(), tabs.length);
        adapter.setFragment(0, new HomeFragment());
        adapter.setFragment(1, new DirectWedgeFragment());

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
}
