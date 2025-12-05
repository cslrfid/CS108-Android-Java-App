package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cslibrary4a.CustomTabAdapter;
import com.csl.cslibrary4a.CustomTabLayout;

public class InventoryFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager2 viewPager;
    CustomTabAdapter adapter;

    private String[] tabs = {"RFID", "Barcode"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        savedInstanceState = null;
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.custom_tabbed_layout2, container, false);
    }

    @Override
    public boolean onMenuItemSelectedA(@NonNull MenuItem item) {
        MainActivity.csLibrary4A.appendToLog("InventoryFragment.onMenuItemSelectedA with viewPager as " + viewPager.getCurrentItem() + ", item as " + item.getItemId());
        InventoryRfidiMultiFragment fragment0 = (InventoryRfidiMultiFragment) adapter.createFragment(0);
        InventoryBarcodeFragment fragment1 = (InventoryBarcodeFragment) adapter.createFragment(1);
        switch (viewPager.getCurrentItem()) {
            case 0:
                return fragment0.onMenuItemSelectedA(item);
            case 1:
                return fragment1.onMenuItemSelectedA(item);
            default:
                return super.onMenuItemSelectedA(item);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        MainActivity.csLibrary4A.appendToLog("InventoryFragment.onViewCreated: going to addMenuProvider");
        getActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@org.jspecify.annotations.NonNull Menu menu, @org.jspecify.annotations.NonNull MenuInflater menuInflater) {
                MainActivity.csLibrary4A.appendToLog("InventoryFragment.onViewCreated.onCreateMenu");
                onCreateMenuA(menu, menuInflater);
            }

            @Override
            public boolean onMenuItemSelected(@org.jspecify.annotations.NonNull MenuItem item) {
                MainActivity.csLibrary4A.appendToLog("InventoryFragment.onViewCreated.onMenuItemSelected");
                return onMenuItemSelectedA(item);
            }
        }, getViewLifecycleOwner());
        super.onViewCreated(view, savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_inv);
        actionBar.setTitle(R.string.title_activity_inventory);

        adapter = new CustomTabAdapter(this, tabs.length);
        adapter.setFragment(0, InventoryRfidiMultiFragment.newInstance(false, null, null));
        adapter.setFragment(1, new InventoryBarcodeFragment());

        viewPager = (ViewPager2) getActivity().findViewById(R.id.OperationsPager2);
        Log.i("Hello", "InventoryFragment.onActivity");
        viewPager.setAdapter(adapter);
        //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        CustomTabLayout tabLayout = (CustomTabLayout) getActivity().findViewById(R.id.OperationsTabLayout2);
        tabLayout.addTab(tabs, viewPager);
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
