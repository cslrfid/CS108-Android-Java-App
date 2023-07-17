package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.csl.cs108ademoapp.adapters.ColdChainAdapter;
import com.csl.cs108ademoapp.R;

public class ColdChainFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager viewPager;
    ColdChainAdapter mAdapter;

    private String[] tabs = {"Select Tag", "One-shot", "Logging"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.custom_tabbed_layout, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        InventoryRfidiMultiFragment fragment1 = (InventoryRfidiMultiFragment) mAdapter.fragment0;
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
        actionBar.setTitle(R.string.title_activity_coldChain);

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);

        mAdapter = new ColdChainAdapter(getActivity().getSupportFragmentManager());
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
        if (mAdapter.fragment2 != null) mAdapter.fragment2.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        if (mAdapter.fragment0 != null) mAdapter.fragment0.onStop();
        if (mAdapter.fragment1 != null) mAdapter.fragment1.onStop();
        if (mAdapter.fragment2 != null) mAdapter.fragment2.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mAdapter.fragment0 != null) mAdapter.fragment0.onDestroyView();
        if (mAdapter.fragment1 != null) mAdapter.fragment1.onDestroyView();
        if (mAdapter.fragment2 != null) mAdapter.fragment2.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mAdapter.fragment0 != null) mAdapter.fragment0.onDestroy();
        if (mAdapter.fragment1 != null) mAdapter.fragment1.onDestroy();
        if (mAdapter.fragment2 != null) mAdapter.fragment2.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        if (mAdapter.fragment0 != null) mAdapter.fragment0.onDetach();
        if (mAdapter.fragment1 != null) mAdapter.fragment1.onDetach();
        if (mAdapter.fragment2 != null) mAdapter.fragment2.onDetach();
        super.onDetach();
    }

    public ColdChainFragment() {
        super("ColdChainFragment");
    }
}
