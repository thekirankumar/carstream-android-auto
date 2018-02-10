package com.thekirankumar.youtubeauto.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SettingsUtils {

    public static boolean isDisabledNotifications(Context context) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (sharedPrefs != null) {
            return sharedPrefs.getBoolean("pref_disable_notifications", false);
        }
        return false;
    }

}
