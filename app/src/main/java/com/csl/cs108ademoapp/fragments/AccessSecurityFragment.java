package com.csl.cs108ademoapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.csl.cs108ademoapp.MainActivity;
import com.csl.cs108ademoapp.R;
import com.csl.cslibrary4a.CustomTabAdapter;
import com.csl.cslibrary4a.CustomTabLayout;

public class AccessSecurityFragment extends CommonFragment {
    private ActionBar actionBar;
    private ViewPager2 viewPager;
    CustomTabAdapter adapter;

    private String[] tabs = {"Lock", "Kill"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.custom_tabbed_layout2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        menuFragment = true;
        super.onViewCreated(view, savedInstanceState);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setIcon(R.drawable.dl_access);
        actionBar.setTitle(R.string.title_activity_security);

        adapter = new CustomTabAdapter(this, tabs.length);
        adapter.setFragment(0, new AccessSecurityLockFragment());
        adapter.setFragment(1, new AccessSecurityKillFragment());

        viewPager = (ViewPager2) view.findViewById(R.id.OperationsPager2);
        viewPager.setAdapter(adapter);

        CustomTabLayout tabLayout = (CustomTabLayout) view.findViewById(R.id.OperationsTabLayout2);
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
        if (MainActivity.csLibrary4A != null) {
            MainActivity.csLibrary4A.setSameCheck(true);
            MainActivity.csLibrary4A.restoreAfterTagSelect();
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        adapter.fragment0.onDetach();
        adapter.fragment1.onDetach();
        super.onDetach();
    }

    public AccessSecurityFragment() {
        super("AccessSecurityFragment");
    }
}
