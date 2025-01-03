package com.csl.cs108ademoapp.fragments;

import static com.csl.cslibrary4a.RfidReader.TagType.TAG_EM_COLDCHAIN;

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
import com.csl.cslibrary4a.AdapterTab;
import com.google.android.material.tabs.TabLayout;

public class ColdChainFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager viewPager;
    AdapterTab adapter;

    private String[] tabs = {"Select Tag", "Logging", "One-shot"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState, true);
        return inflater.inflate(R.layout.custom_tabbed_layout, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        } else return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_inv);
        actionBar.setTitle(R.string.title_activity_coldChain);

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);

        adapter = new AdapterTab(getActivity().getSupportFragmentManager(), tabs.length);
        adapter.setFragment(0, InventoryRfidiMultiFragment.newInstance(true, TAG_EM_COLDCHAIN, "E280B0"));
        adapter.setFragment(1, new AccessColdChainFragment());
        adapter.setFragment(2, new AccessEm4325PassiveFragment());

        viewPager = (ViewPager) getActivity().findViewById(R.id.OperationsPager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        for (int i = 0; i < tabs.length; i++) {
            if (MainActivity.csLibrary4A.get98XX() == 2 && i == tabs.length -1) break;;
            String tab_name = tabs[i];
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
