package com.csl.cs108ademoapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to hold the data for Navigation Drawer Items
 */
public class DrawerListContent {
    //An array of sample (Settings) items.
    public static List<DrawerItem> ITEMS = new ArrayList<>();

    //A map of sample (Settings) items, by ID.
    public static Map<String, DrawerItem> ITEM_MAP = new HashMap<>();

    public enum DrawerPositions {
        MAIN, SPECIAL,
        ABOUT, CONNECT,
        INVENTORY, SEARCH, MULTIBANK,
        SETTING, FILTER, READWRITE, SECURITY,

        IMPINVENTORY, IMP775, IMPAUTOTUNE,
        ALIEN,
        UCODE8, UCODEDNA,
        BAPCARD, COLDCHAIN, AURASENSE,
        KILOWAY,
        LONGJING,
        AXZON, RFMICRON,
        FDMICRO,
        CTESIUS,
        ASYGNTAG,

        REGISTER, READWRITEUSER, WEDGE, DIRECTWEDGE, SIMINVENTORY,
        BLANK;

        public static DrawerPositions toDrawerPosition(int x) {
            switch(x) {
                case 0: return ABOUT;
                case 1: return CONNECT;
                case 2: return INVENTORY;
                case 3: return SEARCH;
                case 4: return MULTIBANK;
                case 5: return SIMINVENTORY;
                case 6: return SETTING;
                case 7: return FILTER;
                case 8: return READWRITE;
                case 9: return SECURITY;

                case 10: return IMPINVENTORY;
                case 11: return ALIEN;
                case 12: return UCODE8;
                case 13: return UCODEDNA;
                case 14: return BAPCARD;
                case 15: return COLDCHAIN;
                case 16: return AURASENSE;
                case 17: return KILOWAY;
                case 18: return LONGJING;
                case 19: return AXZON;
                //case 15: return RFMICRON;
                case 20: return FDMICRO;
                case 21: return CTESIUS;
                case 22: return ASYGNTAG;

                case 23: return REGISTER;
                case 24: return READWRITEUSER;
                case 25: return WEDGE;
                case 26: return DIRECTWEDGE;
            }
            return null;
        }
    }

    static {
        // Add items.
        addItem(new DrawerItem("0", "About", R.drawable.dl_about));
        addItem(new DrawerItem("1", "Connect", R.drawable.dl_rdl));
        addItem(new DrawerItem("2", "Inventory", R.drawable.dl_inv));
        addItem(new DrawerItem("3", "Geiger Search", R.drawable.dl_loc));
        addItem(new DrawerItem("4", "Multi-bank Inventory", R.drawable.dl_inv));
        addItem(new DrawerItem("5", "Simple Inventory", R.drawable.dl_rr));
        addItem(new DrawerItem("6", "Settings", R.drawable.dl_sett));
        addItem(new DrawerItem("7", "Filters", R.drawable.dl_filters));
        addItem(new DrawerItem("8", "Read/Write", R.drawable.dl_access));
        addItem(new DrawerItem("9", "Security", R.drawable.dl_access));

        addItem(new DrawerItem("10", "Impinj Special Features", R.drawable.dl_loc));
        addItem(new DrawerItem("11", "Alien", R.drawable.dl_loc));
        addItem(new DrawerItem("12", "NXP UCODE 8", R.drawable.dl_loc));
        addItem(new DrawerItem("13", "NXP UCODE DNA", R.drawable.dl_loc));
        addItem(new DrawerItem("14", "uEm CS9010 BAP ID Card", R.drawable.dl_loc));
        addItem(new DrawerItem("15", "uEm Cold Chain CS8300", R.drawable.dl_loc));
        addItem(new DrawerItem("16", "uEm Aura-sense", R.drawable.dl_loc));
        addItem(new DrawerItem("17", "Kiloway KX2005X-BL", R.drawable.dl_rr));
        addItem(new DrawerItem("18", "Axzon", R.drawable.dl_loc));
        addItem(new DrawerItem("19", "FM13DT160", R.drawable.dl_loc));
        addItem(new DrawerItem("20", "Landa CTESIUS", R.drawable.dl_loc));
        addItem(new DrawerItem("21", "Asygn AS321x", R.drawable.dl_loc));

        addItem(new DrawerItem("22", "Register Tag", R.drawable.dl_rr));
        addItem(new DrawerItem("23", "Large sized memory read/write", R.drawable.dl_rr));
        addItem(new DrawerItem("24", "Wedge", R.drawable.dl_rr));
        addItem(new DrawerItem("25", "Direct Wedge", R.drawable.dl_rr));
    }

    private static void addItem(DrawerItem item) {

        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class DrawerItem {
        public String id;
        public String content;
        public int icon;

        public DrawerItem(String id, String content, int icon_id) {
            this.id = id;
            this.content = content;
            this.icon = icon_id;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
