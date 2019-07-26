package io.monke.app.settings.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import dagger.android.support.AndroidSupportInjection;
import io.monke.app.R;
import io.monke.app.internal.helpers.IntentHelper;
import io.monke.app.splash.ui.SplashActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

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

        findPreference("pref_day_night").setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {

                Toast.makeText(getActivity(), "Restarting application in 1 second to apply new theme", Toast.LENGTH_LONG).show();

                Observable.timer(1, TimeUnit.SECONDS)
                        .observeOn(Schedulers.io())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(res -> {
                            Intent mStartActivity = new Intent(getActivity(), SplashActivity.class);
                            int mPendingIntentId = 123456;
                            PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), mPendingIntentId, mStartActivity,
                                    PendingIntent.FLAG_CANCEL_CURRENT);
                            AlarmManager mgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                            System.exit(0);
                        });

                return true;
            }
        });

        findPreference("set_rate_app").setOnPreferenceClickListener(pref -> {
            if (getActivity() == null) return false;

            final String appPackageName = getActivity().getPackageName(); // getPackageName() place Context or Activity object
            try {
                getActivity().startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + appPackageName)
                ));
            } catch (ActivityNotFoundException ex) {
                // если вдруг стора нет в телефоне
                getActivity().startActivity(new Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)
                ));
            }
            return true;
        });

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
