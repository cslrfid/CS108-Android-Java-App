package com.csl.cs108ademoapp.fragments;

import static com.csl.cslibrary4a.RfidReader.TagType.TAG_FDMICRO;

import android.os.Bundle;

import com.csl.cs108ademoapp.MainActivity;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.csl.cs108ademoapp.R;

public class FdmicroFragment1 extends CommonFragment {
    FragmentActivity context1;
    private ActionBar actionBar;
    private ViewPager2 viewPager;
    ScreenSlidePagerAdapter adapter;

    private String[] tabs = {"Scan", "Configuration"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.custom_tabbed_layout2, container, false);
    }

    @Override
    public boolean onMenuItemSelectedA(MenuItem item) {
/*
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
        } else return super.onMenuItemSelectedA(item);
*/
        return true;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        MainActivity.csLibrary4A.appendToLog("FdmicroFragment.onViewCreated: going to addMenuProvider");
        getActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@org.jspecify.annotations.NonNull Menu menu, @org.jspecify.annotations.NonNull MenuInflater menuInflater) {
                onCreateMenuA(menu, menuInflater);
            }

            @Override
            public boolean onMenuItemSelected(@org.jspecify.annotations.NonNull MenuItem item) {
                return onMenuItemSelectedA(item);
            }
        }, getViewLifecycleOwner());
        super.onViewCreated(view, savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_inv);
        actionBar.setTitle(R.string.title_activity_fdMicro);

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.OperationsTabLayout);
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

        adapter = new ScreenSlidePagerAdapter(getParentFragmentManager(), getLifecycle());
        //adapter.setFragment(0, InventoryRfidiMultiFragment.newInstance(true, TAG_FDMICRO, "")); //""E2827001"));
        //adapter.setFragment(1, new AccessFdmicroFragment());

        viewPager = (ViewPager2) getActivity().findViewById(R.id.OperationsPager2);
        viewPager.setSaveFromParentEnabled(false);
        viewPager.setAdapter(adapter);
//        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        MainActivity.csLibrary4A.appendToLog("FdmicroFragment.onViewCreated: End");
    }

    @Override
    public void onPause() {
        //adapter.fragment0.onPause();
        //adapter.fragment1.onPause();
        super.onPause();
    }

    @Override
    public void onStop() {
        //adapter.fragment0.onStop();
        //adapter.fragment1.onStop();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        //adapter.fragment0.onDestroyView();
        //adapter.fragment1.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        //adapter.fragment0.onDestroy();
        //adapter.fragment1.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        //adapter.fragment0.onDetach();
        //adapter.fragment1.onDetach();
        super.onDetach();
    }

    public FdmicroFragment1(FragmentActivity context) {
        super("FdmicroFragment");
        this.context1 = context;
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @Override
        public Fragment createFragment(int position) {
            MainActivity.csLibrary4A.appendToLog("FdmicroFragment.ScreenSlidePagerAdapter.createFragment: position 0");
            switch (position) {
                case 0:
                    return InventoryRfidiMultiFragment.newInstance(true, TAG_FDMICRO, "");
                case 1:
                    return new AccessFdmicroFragment();
            }
            return null;
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
