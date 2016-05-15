package com.patrick.refundly.controllers;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.patrick.refundly.R;

/*
 * SKAL NOK LAVES OM FRA BUNDEN - VI HAR INGEN GRUND TIL AT BRUGE PREFERENCEFRAGMENT!
 * Umiddelbart behøver vi ikke gemme ting i sharedpreferences, men kan nøjes med at bruge
 * det brugerobjekt vi har liggende i Controlleren.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }

    @Override
    public void onResume() {
        super.onResume();
        // Set title
        getActivity()
                .setTitle(R.string.settings_title);
    }

}
