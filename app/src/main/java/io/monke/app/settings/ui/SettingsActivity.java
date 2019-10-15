package io.monke.app.settings.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.monke.app.R;
import io.monke.app.internal.BaseMvpInjectActivity;
import io.monke.app.internal.helpers.IntentHelper;
import io.monke.app.internal.views.list.BorderedItemSeparator;
import io.monke.app.settings.contract.SettingsView;
import io.monke.app.settings.views.SettingsPresenter;
import io.monke.app.setup.ui.ChangeWalletBottomDialog;
import io.monke.app.setup.ui.DepositBottomDialog;
import io.monke.app.splash.ui.SplashActivity;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;
import network.minter.core.MinterSDK;
import network.minter.explorer.MinterExplorerApi;

import static io.monke.app.internal.helpers.MathHelper.bdHuman;

public class SettingsActivity extends BaseMvpInjectActivity implements SettingsView {

    @Inject Provider<SettingsPresenter> presenterProvider;
    @InjectPresenter SettingsPresenter presenter;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.list) RecyclerView list;
    private BottomSheetDialogFragment mDialog;

    @Override
    public void setBalance(BigDecimal totalBalance) {
        toolbar.setTitle(String.format("%s %s", bdHuman(totalBalance), MinterSDK.DEFAULT_COIN));
    }

    @Override
    public void setDelegatedBalance(BigDecimal delegatedAmount) {
        toolbar.setSubtitle(String.format("%s %s", bdHuman(delegatedAmount), MinterSDK.DEFAULT_COIN));
    }

    @Override
    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        list.setLayoutManager(new LinearLayoutManager(this));
        list.addItemDecoration(new BorderedItemSeparator(this, R.drawable.shape_bottom_separator, false, false));
        list.setAdapter(adapter);
    }

    @Override
    public void startRateApp() {
        final String appPackageName = getPackageName(); // getPackageName() place Context or Activity object
        try {
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + appPackageName)
            ));
        } catch (ActivityNotFoundException ex) {
            // если вдруг стора нет в телефоне
            startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)
            ));
        }
    }

    @Override
    public void startBackup() {
        startActivity(new Intent(this, BackupSeedActivity.class));
    }

    @Override
    public void startAbout() {
        startActivity(IntentHelper.newUrl("https://monke.io"));
    }

    @Override
    public void startTelegram() {
        startActivity(IntentHelper.newUrl("https://t.me/MonkeApp"));
    }

    @Override
    public void restartApplication() {
        Toast.makeText(this, "Restarting application in 1 second to apply new theme", Toast.LENGTH_LONG).show();

        Observable.timer(1, TimeUnit.SECONDS)
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    Intent mStartActivity = new Intent(this, SplashActivity.class);
                    int mPendingIntentId = 123456;
                    PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId, mStartActivity,
                            PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                    System.exit(0);
                });
    }

    @Override
    public void startDonationDialog(Spanned title, String address, String qrPath) {
        if (mDialog != null) {
            try {
                mDialog.dismiss();
            } catch (Throwable ignore) {
            }
            mDialog = null;
        }

        mDialog = new DepositBottomDialog.Builder(false)
                .setTitle(title)
                .setAddress(address)
                .setQRPath(qrPath)
                .setShowAddressIcon(true)
                .build();

        mDialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void startTransactionsList(String address) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(MinterExplorerApi.FRONT_URL + "/address/" + address + "?active_tab=tx")));
    }

    @Override
    public void startChangeWalletDialog(ChangeWalletBottomDialog.OnWalletChangedListener listener) {
        if (mDialog != null) {
            try {
                mDialog.dismiss();
            } catch (Throwable ignore) {
            }
            mDialog = null;
        }
        mDialog = ChangeWalletBottomDialog.newInstance();
        ((ChangeWalletBottomDialog) mDialog).setOnWalletChangedListener(listener);
        mDialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void startIntent(Intent intent) {
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_deposit) {
            mDialog = DepositBottomDialog.newInstance(false);
            mDialog.show(getSupportFragmentManager(), null);
        }

        return false;
    }

    @ProvidePresenter
    SettingsPresenter providePresenter() {
        return presenterProvider.get();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        toolbar.inflateMenu(R.menu.settings_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_deposit) {
                mDialog = DepositBottomDialog.newInstance(false);
                mDialog.show(getSupportFragmentManager(), null);
            }

            return false;
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog != null) {
            try {
                mDialog.dismiss();
                mDialog = null;
            } catch (Throwable ignore) {
            }
        }
    }
}
