package com.patrick.refundly.view;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;

import com.patrick.refundly.R;

/**
 * Created by patrick on 3/30/16.
 */
public class SettingsScreen extends PreferenceActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        setTheme(R.style.PreferenceStyle);
    }


}
