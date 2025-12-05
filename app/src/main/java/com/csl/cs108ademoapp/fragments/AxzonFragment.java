package com.csl.cs108ademoapp.fragments;

import static com.csl.cslibrary4a.RfidReader.TagType.TAG_AXZON;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import android.os.Bundle;

import com.csl.cslibrary4a.CustomTabAdapter;
import com.csl.cslibrary4a.CustomTabLayout;
import com.csl.cslibrary4a.RfidReader;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

public class AxzonFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager2 viewPager;
    CustomTabAdapter adapter;
    private String[] tabs = { "Read", "Scan/Select", "Logger", "Security" };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.custom_tabbed_layout2, container, false);
    }

    @Override
    public boolean onMenuItemSelectedA(MenuItem item) {
        InventoryRfidiMultiFragment fragment = (InventoryRfidiMultiFragment) adapter.fragment1;
        if (item.getItemId() == R.id.menuAction_clear) {
            fragment.clearTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_sortRssi) {
            fragment.sortTagsListByRssi();
            return true;
        } else if (item.getItemId() == R.id.menuAction_sort) {
            fragment.sortTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_save) {
            fragment.saveTagsList();
            return true;
        } else if (item.getItemId() == R.id.menuAction_share) {
            fragment.shareTagsList();
            return true;
        } else return super.onMenuItemSelectedA(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        MainActivity.csLibrary4A.appendToLog("AxzonFragment.onViewCreated: going to addMenuProvider");
        getActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@org.jspecify.annotations.NonNull Menu menu, @org.jspecify.annotations.NonNull MenuInflater menuInflater) {
                MainActivity.csLibrary4A.appendToLog("AxzonFragment.onViewCreated.onCreateMenu");
                onCreateMenuA(menu, menuInflater);
            }

            @Override
            public boolean onMenuItemSelected(@org.jspecify.annotations.NonNull MenuItem item) {
                MainActivity.csLibrary4A.appendToLog("AxzonFragment.onViewCreated.onMenuItemSelected");
                return onMenuItemSelectedA(item);
            }
        }, getViewLifecycleOwner());
        super.onViewCreated(view, savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_inv);
        if (true) actionBar.setTitle(R.string.title_activity_axzon);
        else {
            String stringTitle = getResources().getString(R.string.title_activity_axzon);
            if (MainActivity.tagType == RfidReader.TagType.TAG_MAGNUS_S2) stringTitle = "S2";
            else if (MainActivity.tagType == RfidReader.TagType.TAG_MAGNUS_S3) stringTitle = "S3";
            if (MainActivity.tagType == RfidReader.TagType.TAG_AXZON_XERXES) stringTitle = "Xerxes";
            actionBar.setTitle(stringTitle);
         }

        adapter = new CustomTabAdapter(this, tabs.length);
        adapter.setFragment(0, AccessMicronFragment.newInstance(true));
        adapter.setFragment(1, InventoryRfidiMultiFragment.newInstance(true, TAG_AXZON, ""));
        adapter.setFragment(2, new AccessOpusLoggerFragment());
        adapter.setFragment(3, new AccessUcodeFragment());

        viewPager = (ViewPager2) getActivity().findViewById(R.id.OperationsPager2);
        viewPager.setAdapter(adapter);
        //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        CustomTabLayout tabLayout = (CustomTabLayout) getActivity().findViewById(R.id.OperationsTabLayout2);
        tabLayout.addTab(tabs, viewPager);

        MainActivity.csLibrary4A.setBasicCurrentLinkProfile();
    }

    @Override
    public void onPause() {
        if (adapter.fragment0 != null) if (adapter.fragment0.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment0.onPause();
        if (adapter.fragment1 != null) if (adapter.fragment1.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment1.onPause();
        if (adapter.fragment2 != null) if (adapter.fragment2.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment2.onPause();
        if (adapter.fragment3 != null) if (adapter.fragment3.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment3.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (adapter.fragment0 != null) if (adapter.fragment0.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment0.onStop();
        if (adapter.fragment1 != null) if (adapter.fragment1.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment1.onStop();
        if (adapter.fragment2 != null) if (adapter.fragment2.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment2.onStop();
        if (adapter.fragment3 != null) if (adapter.fragment3.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment3.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (adapter.fragment0 != null) if (adapter.fragment0.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment0.onDestroyView();
        if (adapter.fragment1 != null) if (adapter.fragment1.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment1.onDestroyView();
        if (adapter.fragment2 != null) if (adapter.fragment2.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment2.onDestroyView();
        if (adapter.fragment3 != null) if (adapter.fragment3.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment3.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (adapter.fragment0 != null) if (adapter.fragment0.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment0.onDestroy();
        if (adapter.fragment1 != null) if (adapter.fragment1.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment1.onDestroy();
        if (adapter.fragment2 != null) if (adapter.fragment2.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment2.onDestroy();
        if (adapter.fragment3 != null) if (adapter.fragment3.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment3.onDestroy();
        if (MainActivity.csLibrary4A != null) {
            if (MainActivity.selectFor != -1) {
                MainActivity.csLibrary4A.setSelectCriteriaDisable(-1);
                MainActivity.selectFor = -1;
            }
            MainActivity.csLibrary4A.restoreAfterTagSelect();
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (adapter.fragment0 != null) if (adapter.fragment0.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment0.onDetach();
        if (adapter.fragment1 != null) if (adapter.fragment1.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment1.onDetach();
        if (adapter.fragment2 != null) if (adapter.fragment2.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment2.onDetach();
        if (adapter.fragment3 != null) if (adapter.fragment3.getLifecycle().getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) adapter.fragment3.onDetach();
        super.onDetach();
    }

    public AxzonFragment() { super("AxzonFragment"); }
}
