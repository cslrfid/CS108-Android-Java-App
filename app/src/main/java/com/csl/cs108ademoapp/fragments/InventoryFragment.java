package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cslibrary4a.AdapterTab;
import com.google.android.material.tabs.TabLayout;

public class InventoryFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager viewPager;
    AdapterTab adapter;

    private String[] tabs = {"RFID", "Barcode"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        savedInstanceState = null;
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.custom_tabbed_layout, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MainActivity.csLibrary4A.appendToLog("InventoryFragment: onOptionsItemSelected with viewPager as " + viewPager.getCurrentItem());
        InventoryRfidiMultiFragment fragment0 = (InventoryRfidiMultiFragment) adapter.getItem(0);
        InventoryBarcodeFragment fragment1 = (InventoryBarcodeFragment) adapter.getItem(1);
        switch (viewPager.getCurrentItem()) {
            case 0:
                return fragment0.onOptionsItemSelected(item);
            case 1:
                return fragment1.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_inv);
        actionBar.setTitle(R.string.title_activity_inventory);

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);

        adapter = new AdapterTab(getActivity().getSupportFragmentManager(), tabs.length);
        adapter.setFragment(0, InventoryRfidiMultiFragment.newInstance(false, null, null));
        adapter.setFragment(1, new InventoryBarcodeFragment());

        viewPager = (ViewPager) getActivity().findViewById(R.id.OperationsPager);
        Log.i("Hello", "InventoryFragment.onActivity");
        viewPager.setAdapter(adapter);
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

    public InventoryFragment() {
        super("InventoryFragment");
    }
}
