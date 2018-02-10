package com.thekirankumar.youtubeauto.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.thekirankumar.youtubeauto.R;


public class SettingsPhoneFragment extends PreferenceFragment {

    public SettingsPhoneFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}
