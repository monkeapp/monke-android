package io.monke.app.settings.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import io.monke.app.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);


//        findPreference("pref_address_list").setOnPreferenceClickListener(pref->{
//
//
//            return false;
//        });
    }
}
