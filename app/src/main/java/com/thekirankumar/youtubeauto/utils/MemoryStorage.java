package com.thekirankumar.youtubeauto.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

public class MemoryStorage {

    public static final String SD_CARD = "sdCard";
    public static final String EXTERNAL_SD_CARD = "externalSdCard";
    public static final String ENV_SECONDARY_STORAGE = "SECONDARY_STORAGE";

    private MemoryStorage() {
    }

    /**
     * @return True if the external storage is available. False otherwise.
     */
    public static boolean isAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    public static String[] getStorageDirectories(Context pContext)
    {
        // Final set of paths
        final Set<String> rv = new HashSet<>();

        //Get primary & secondary external device storage (internal storage & micro SDCARD slot...)
        File[]  listExternalDirs = ContextCompat.getExternalFilesDirs(pContext, null);
        for(int i=0;i<listExternalDirs.length;i++){
            if(listExternalDirs[i] != null) {
                String path = listExternalDirs[i].getAbsolutePath();
                int indexMountRoot = path.indexOf("/Android/data/");
                if(indexMountRoot >= 0 && indexMountRoot <= path.length()){
                    //Get the root path for the external directory
                    rv.add(path.substring(0, indexMountRoot));
                }
            }
        }
        return rv.toArray(new String[rv.size()]);
    }

    public static String getSdCardPath() {
        return Environment.getExternalStorageDirectory().getPath() + "/";
    }

    /**
     * @return True if the external storage is writable. False otherwise.
     */
    public static boolean isWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;

    }
}