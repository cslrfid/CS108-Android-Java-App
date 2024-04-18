package com.csl.cslibrary4a;

import android.content.Context;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileData {
    File file;
    Context context; Utility utility;
    public FileData(Context context, Utility utility) {
        this.context = context;
        this.utility = utility;
    }
}
