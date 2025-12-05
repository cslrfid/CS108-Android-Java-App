package com.csl.cslibrary4a;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

public class CustomTabLayout extends TabLayout {

    public CustomTabLayout(@NonNull Context context) {
        super(context);
        init();
    }
    public CustomTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public CustomTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
    }

    public void addTab(String[] tabs, ViewPager2 viewPager2) {
        appendToLog("CustomTabLayout.addTab: viewPager2 is " + (viewPager2 == null ? "null" : "valid"));
        for (String tab_name : tabs) {
            addTab(newTab().setText(tab_name));
        }
        addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                appendToLog("CustomTabLayout.addTab.onTabSelectedListener.onTabSelected with tab.getPosition() = " + tab.getPosition());
                viewPager2.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                appendToLog("CustomTabLayout.addTab.onTabSelectedListener.onTabUnselected with tab.getPosition() = " + tab.getPosition());
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                appendToLog("CustomTabLayout.addTab.onTabSelectedListener.onTabReselected with tab.getPosition() = " + tab.getPosition());
            }
        });
    }

    void appendToLog(String string) {
        Log.i("Hello", string);
    }
}
