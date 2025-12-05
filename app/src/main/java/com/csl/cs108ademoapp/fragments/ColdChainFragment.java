package com.csl.cs108ademoapp.fragments;

import static com.csl.cslibrary4a.RfidReader.TagType.TAG_EM_COLDCHAIN;

import android.os.Bundle;
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

public class ColdChainFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager2 viewPager;
    CustomTabAdapter adapter;

    private String[] tabs = {"Select Tag", "Logging", "One-shot"};
    private String[] tabs2 = {"Select Tag", "Logging"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.custom_tabbed_layout2, container, false);
    }

    @Override
    public boolean onMenuItemSelectedA(MenuItem item) {
        InventoryRfidiMultiFragment fragment1 = (InventoryRfidiMultiFragment) adapter.fragment0;
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
        } else return super.onMenuItemSelectedA(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        MainActivity.csLibrary4A.appendToLog("ColdChainFragment.onViewCreated: going to addMenuProvider");
        getActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@org.jspecify.annotations.NonNull Menu menu, @org.jspecify.annotations.NonNull MenuInflater menuInflater) {
                MainActivity.csLibrary4A.appendToLog("ColdChainFragment.onViewCreated.onCreateMenu");
                onCreateMenuA(menu, menuInflater);
            }

            @Override
            public boolean onMenuItemSelected(@org.jspecify.annotations.NonNull MenuItem item) {
                MainActivity.csLibrary4A.appendToLog("ColdChainFragment.onViewCreated.onMenuItemSelected");
                return onMenuItemSelectedA(item);
            }
        }, getViewLifecycleOwner());
        super.onViewCreated(view, savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_inv);
        actionBar.setTitle(R.string.title_activity_coldChain);

        adapter = new CustomTabAdapter(this, tabs.length);
        adapter.setFragment(0, InventoryRfidiMultiFragment.newInstance(true, TAG_EM_COLDCHAIN, "" /*"E280B0"*/));
        adapter.setFragment(1, new AccessColdChainFragment());
        adapter.setFragment(2, new AccessEm4325PassiveFragment());

        viewPager = (ViewPager2) view.findViewById(R.id.OperationsPager2);
        viewPager.setAdapter(adapter);
        //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        CustomTabLayout tabLayout = (CustomTabLayout) view.findViewById(R.id.OperationsTabLayout2);
        tabLayout.addTab(MainActivity.csLibrary4A.get98XX() == 2 ? tabs2 : tabs, viewPager);
    }

    @Override
    public void onPause() {
        if (adapter.fragment0 != null) adapter.fragment0.onPause();
        if (adapter.fragment1 != null) adapter.fragment1.onPause();
        if (adapter.fragment2 != null) adapter.fragment2.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (adapter.fragment0 != null) adapter.fragment0.onStop();
        if (adapter.fragment1 != null) adapter.fragment1.onStop();
        if (adapter.fragment2 != null) adapter.fragment2.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (adapter.fragment0 != null) adapter.fragment0.onDestroyView();
        if (adapter.fragment1 != null) adapter.fragment1.onDestroyView();
        if (adapter.fragment2 != null) adapter.fragment2.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (adapter.fragment0 != null) adapter.fragment0.onDestroy();
        if (adapter.fragment1 != null) adapter.fragment1.onDestroy();
        if (adapter.fragment2 != null) adapter.fragment2.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (adapter.fragment0 != null) adapter.fragment0.onDetach();
        if (adapter.fragment1 != null) adapter.fragment1.onDetach();
        if (adapter.fragment2 != null) adapter.fragment2.onDetach();
        super.onDetach();
    }

    public ColdChainFragment() {
        super("ColdChainFragment");
    }
}
