package io.monke.app.settings.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceFragmentCompat;
import dagger.android.support.AndroidSupportInjection;
import io.monke.app.R;
import io.monke.app.internal.helpers.IntentHelper;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Inject SharedPreferences prefs;

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        findPreference("set_backup_mnemonic").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), BackupSeedActivity.class));
            return false;
        });

        findPreference("set_about").setOnPreferenceClickListener(pref -> {
            startActivity(IntentHelper.newUrl("https://monke.io"));
            return false;
        });

        findPreference("set_tg_channel").setOnPreferenceClickListener(pref -> {
            startActivity(IntentHelper.newUrl("https://t.me/MonkeApp"));
            return false;
        });
    }
}
