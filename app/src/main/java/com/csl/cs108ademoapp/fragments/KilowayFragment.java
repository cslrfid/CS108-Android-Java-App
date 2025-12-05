package com.csl.cs108ademoapp.fragments;

import static com.csl.cslibrary4a.RfidReader.TagType.TAG_KILOWAY;

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

public class KilowayFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager2 viewPager;
    CustomTabAdapter adapter;

    private String[] tabs = {"Scan", "Geiger"}; //, "Access"};
    int iTargetOld, iSessionOld;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.custom_tabbed_layout2, container, false);
    }

    @Override
    public boolean onMenuItemSelectedA(MenuItem item) {
        InventoryRfidiMultiFragment fragment = (InventoryRfidiMultiFragment) adapter.createFragment(0);
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
        MainActivity.csLibrary4A.appendToLog("KilowayFragment.onViewCreated: going to addMenuProvider");
        getActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@org.jspecify.annotations.NonNull Menu menu, @org.jspecify.annotations.NonNull MenuInflater menuInflater) {
                MainActivity.csLibrary4A.appendToLog("KilowayFragment.onViewCreated.onCreateMenu");
                onCreateMenuA(menu, menuInflater);
            }
            @Override
            public boolean onMenuItemSelected(@org.jspecify.annotations.NonNull MenuItem item) {
                MainActivity.csLibrary4A.appendToLog("KilowayFragment.onViewCreated.onMenuItemSelected");
                return onMenuItemSelectedA(item);
            }
        }, getViewLifecycleOwner());
        super.onViewCreated(view, savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_inv);
        actionBar.setTitle(R.string.title_activity_kiloway);

        adapter = new CustomTabAdapter(this, tabs.length);
        adapter.setFragment(0, InventoryRfidiMultiFragment.newInstance(true, TAG_KILOWAY, "" /*""E281D"*/));
        adapter.setFragment(1, new InventoryRfidSearchFragment(true));
        adapter.setFragment(2, new AccessKilowayFragment(false));

        viewPager = (ViewPager2) getActivity().findViewById(R.id.OperationsPager2);
        viewPager.setAdapter(adapter);
        //viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        CustomTabLayout tabLayout = (CustomTabLayout) getActivity().findViewById(R.id.OperationsTabLayout2);
        tabLayout.addTab(tabs, viewPager);

        iTargetOld = MainActivity.csLibrary4A.getQueryTarget();
        iSessionOld = MainActivity.csLibrary4A.getQuerySession();
        MainActivity.csLibrary4A.setBasicCurrentLinkProfile();
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
        if (MainActivity.csLibrary4A != null) MainActivity.csLibrary4A.setTagGroup(MainActivity.csLibrary4A.getQuerySelect(), iSessionOld, iTargetOld);
        //MainActivity.mCs108Library4a.macWrite(0x203, 0);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        adapter.fragment0.onDetach();
        adapter.fragment1.onDetach();
        super.onDetach();
    }

    public KilowayFragment() {
        super("LedTagFragment");
    }
}
