package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.adapters.MyAdapter;
import com.google.android.material.tabs.TabLayout;

public class SettingFilterFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager viewPager;
    MyAdapter adapter;

    private String[] tabs = {"Pre-filter", "Post-filter", "Rssi-filter"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.custom_tabbed_layout, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_filters);
        actionBar.setTitle(R.string.title_activity_filters);

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);

        adapter = new MyAdapter(getActivity().getSupportFragmentManager(), tabs.length);
        adapter.setFragment(0, new SettingFilterPreFragment());
        adapter.setFragment(1, new SettingFilterPostFragment());
        adapter.setFragment(2, new SettingFilterRssiFragment());

        viewPager = (ViewPager) getActivity().findViewById(R.id.OperationsPager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        for (String tab_name : tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tab_name));
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.i(TAG, "tab.position is " + tab.getPosition());
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

    public SettingFilterFragment() {
        super("SettingFilterFragment");
    }
}