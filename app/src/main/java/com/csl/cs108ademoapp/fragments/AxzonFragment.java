package com.csl.cs108ademoapp.fragments;

import androidx.lifecycle.Lifecycle;
import android.os.Bundle;

import com.csl.cs108ademoapp.adapters.MyAdapter;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;

public class AxzonFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager viewPager;
    MyAdapter adapter;

    private String[] tabs = { "Scan/Select", "Read" };
    private String[] tabsXerxes = { "Logger", "Security" };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.custom_tabbed_layout, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        InventoryRfidiMultiFragment fragment = (InventoryRfidiMultiFragment) adapter.fragment0;
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
        } else return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_inv);
        MainActivity.csLibrary4A.appendToLog("MainActivity.mDid = " + MainActivity.mDid);
        if (false) actionBar.setTitle(R.string.title_activity_axzon);
        else {
            String stringTitle = getResources().getString(R.string.title_activity_axzon);
            if (MainActivity.mDid.matches("E282402")) stringTitle = "S2";
            else if (MainActivity.mDid.matches("E282403")) stringTitle = "S3";
            if (MainActivity.mDid.matches("E282405")) stringTitle = "Xerxes";
            actionBar.setTitle(stringTitle);
         }

        boolean bXervesTag = false;
        if (MainActivity.mDid != null) if (MainActivity.mDid.matches("E282405")) bXervesTag = true;

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);

        adapter = new MyAdapter(getActivity().getSupportFragmentManager(), (bXervesTag ? 4 : 2));
        adapter.setFragment(0, InventoryRfidiMultiFragment.newInstance(true, ""));
        adapter.setFragment(1, AccessMicronFragment.newInstance(true));
        adapter.setFragment(2, new AccessXerxesLoggerFragment());
        adapter.setFragment(3, new AccessUcodeFragment());

        viewPager = (ViewPager) getActivity().findViewById(R.id.OperationsPager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        for (String tab_name : tabs) {
            tabLayout.addTab(tabLayout.newTab().setText(tab_name));
        }
        if (bXervesTag) {
            for (String tab_name : tabsXerxes) {
                tabLayout.addTab(tabLayout.newTab().setText(tab_name));
            }
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
        if (MainActivity.selectFor != -1) {
            MainActivity.csLibrary4A.setSelectCriteriaDisable(-1);
            MainActivity.selectFor = -1;
        }
        MainActivity.csLibrary4A.restoreAfterTagSelect();
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
