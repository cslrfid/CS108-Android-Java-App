package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cs108ademoapp.adapters.MicronAdapter;

public class MicronFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager viewPager;
    MicronAdapter mAdapter;

    private String[] tabs = { "Config/Read", "Select Tag" };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.custom_tabbed_layout, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        InventoryRfidiMultiFragment fragment1 = (InventoryRfidiMultiFragment) mAdapter.fragment1;
        switch (item.getItemId()) {
            case R.id.menuAction_1:
                fragment1.clearTagsList();
                return true;
            case R.id.menuAction_2:
                fragment1.sortTagsList();
                return true;
            case R.id.menuAction_3:
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
        actionBar.setTitle("E"); //"EM Micron");

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);

        MainActivity.mDid = "E28240";
        mAdapter = new MicronAdapter(getActivity().getSupportFragmentManager());
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
        if (mAdapter.fragment0 != null) mAdapter.fragment0.onPause();
        if (mAdapter.fragment1 != null) mAdapter.fragment1.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mAdapter.fragment0 != null) mAdapter.fragment0.onStop();
        if (mAdapter.fragment1 != null) mAdapter.fragment1.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mAdapter.fragment0 != null) mAdapter.fragment0.onDestroyView();
        if (mAdapter.fragment1 != null) mAdapter.fragment1.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (MainActivity.selectFor != -1) {
            MainActivity.mCs108Library4a.setSelectCriteriaDisable(1);
            MainActivity.mCs108Library4a.setSelectCriteriaDisable(2);
            MainActivity.selectFor = -1;
        }
        if (mAdapter.fragment0 != null) mAdapter.fragment0.onDestroy();
        if (mAdapter.fragment1 != null) mAdapter.fragment1.onDestroy();
        MainActivity.mCs108Library4a.appendToLog("Hello4: restoreAfterTagSelect");
        MainActivity.mCs108Library4a.restoreAfterTagSelect();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (mAdapter.fragment0 != null) mAdapter.fragment0.onDetach();
        if (mAdapter.fragment1 != null) mAdapter.fragment1.onDetach();
        super.onDetach();
    }

    public MicronFragment() {
        super("MicronFragment");
    }
}
