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
import com.csl.cs108ademoapp.adapters.LongjingAdapter;
import com.google.android.material.tabs.TabLayout;

public class LongjingFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager viewPager;
    LongjingAdapter mAdapter;

    private String[] tabs = {"Scan", "Geiger"}; //"Access",
    int iTargetOld, iSessionOld;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.custom_tabbed_layout, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        InventoryRfidiMultiFragment fragment1 = (InventoryRfidiMultiFragment) mAdapter.fragment1;
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
        actionBar.setTitle(R.string.title_activity_longjing);

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);

        mAdapter = new LongjingAdapter(getActivity().getSupportFragmentManager());
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

        iTargetOld = MainActivity.csLibrary4A.getQueryTarget();
        iSessionOld = MainActivity.csLibrary4A.getQuerySession();
        MainActivity.csLibrary4A.setBasicCurrentLinkProfile();
    }

    @Override
    public void onPause() {
        mAdapter.fragment0.onPause();
        mAdapter.fragment1.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        mAdapter.fragment0.onStop();
        mAdapter.fragment1.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        mAdapter.fragment0.onDestroyView();
        mAdapter.fragment1.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mAdapter.fragment0.onDestroy();
        mAdapter.fragment1.onDestroy();
        MainActivity.csLibrary4A.setTagGroup(MainActivity.csLibrary4A.getQuerySelect(), iSessionOld, iTargetOld);
        //MainActivity.mCs108Library4a.macWrite(0x203, 0);
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        mAdapter.fragment0.onDetach();
        mAdapter.fragment1.onDetach();
        super.onDetach();
    }

    public LongjingFragment() {
        super("LedTagFragment");
    }
}
