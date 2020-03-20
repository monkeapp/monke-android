package io.monke.app.settings.views;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.widget.CompoundButton;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import io.monke.app.BuildConfig;
import io.monke.app.R;
import io.monke.app.apis.explorer.CachedExplorerAddressRepository;
import io.monke.app.internal.Monke;
import io.monke.app.internal.PrefKeys;
import io.monke.app.internal.data.data.CachedRepository;
import io.monke.app.internal.helpers.HtmlCompat;
import io.monke.app.internal.helpers.QRAddressGenerator;
import io.monke.app.internal.mvp.MvpBasePresenter;
import io.monke.app.internal.views.list.MultiRowAdapter;
import io.monke.app.settings.contract.SettingsView;
import io.monke.app.storage.AccountItem;
import io.monke.app.storage.AccountStorage;
import io.monke.app.storage.AddressAccount;
import io.monke.app.storage.SecretStorage;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import moxy.InjectViewState;
import network.minter.core.MinterSDK;
import network.minter.explorer.models.DelegationInfo;
import network.minter.explorer.models.ExpResult;
import timber.log.Timber;

import static android.content.Context.KEYGUARD_SERVICE;

@InjectViewState
public class SettingsPresenter extends MvpBasePresenter<SettingsView> {

    private final static int REQUEST_AUTH = 1005;
    @Inject SecretStorage secretStorage;
    @Inject CachedRepository<AddressAccount, AccountStorage> accountStorage;
    @Inject
    CachedRepository<ExpResult<List<DelegationInfo>>, CachedExplorerAddressRepository> expAddressRepo;
    @Inject SharedPreferences prefs;
    @Inject Resources res;
    private MultiRowAdapter mAdapter;

    @Inject
    public SettingsPresenter() {
        mAdapter = new MultiRowAdapter();
    }

    @Override
    public void attachView(SettingsView view) {
        super.attachView(view);


        accountStorage.update();
        accountStorage.observe()
                .doOnSubscribe(this::unsubscribeOnDestroy)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(res -> {
                    Timber.d("Loaded balance");
                    AccountItem bip = res.findByCoin(MinterSDK.DEFAULT_COIN);
                    getViewState().setBalance(bip.getBalance());
                });


        expAddressRepo.update();
        expAddressRepo.observe()
                .doOnSubscribe(this::unsubscribeOnDestroy)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(res -> {
                    Timber.d("Loaded delegated");
                    if (res.getMeta().additional != null && res.getMeta().additional.delegatedAmount != null) {
                        getViewState().setDelegatedBalance(res.meta.additional.delegatedAmount);
                    } else {
                        getViewState().setDelegatedBalance(BigDecimal.ZERO);
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_AUTH && resultCode == Activity.RESULT_OK) {
            getViewState().startBackupView();
        }
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        if (!prefs.contains(PrefKeys.QR_TEAM_BITMAP_PATH)) {
            float qrWidth = ((float) Monke.app().display().getWidth()) * 0.388f;
            QRAddressGenerator.create((int) qrWidth, "Mx408fb7d25f40d0361ee370cff812c1fe1fac74a7")
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> {
                        if (res.bitmap == null) {
                            Timber.e("Unable to create QR: Unknown reason.");
                            return;
                        }
                        Timber.d("QR created: %s", res.file.toString());
                        prefs.edit().putString(PrefKeys.QR_TEAM_BITMAP_PATH, res.file.toString()).apply();
                    }, t -> {
                        Timber.e(t, "Unable to generate QR image");
                    });
        }

        SettingsItemRow.ItemData dayNightOpt = new SettingsItemRow.ItemData(0, "Use Night theme", null, null)
                .setSwitchListener(this::onDayNightSwitch)
                .setChecked(() -> prefs.getBoolean(PrefKeys.DAY_NIGHT_THEME, false));

        KeyguardManager km = (KeyguardManager) Monke.app().context().getSystemService(KEYGUARD_SERVICE);

        boolean showBackup = km != null && km.isKeyguardSecure();
        SettingsItemRow.Builder builder = new SettingsItemRow.Builder(Monke.app().context())
                .addItem(dayNightOpt)
                .addItem(R.string.settings_transactions_list, this::onClickTransactions);

        if (showBackup) {
            builder.addItem(R.string.settings_backup_mnemonic, this::onClickBackup);
        }

        builder.addItem(R.string.settings_change_wallet, this::onClickChangeWallet)
                .addItem(R.string.settings_report_problem, this::onClickReport)
                .addItem(R.string.settings_rate_app, this::onClickRate)
                .addItem(R.drawable.ic_make_donation, R.string.settings_make_donation_title, R.string.settings_make_donation_desc, this::onClickDonate)
                .addItem(R.drawable.ic_buy_banana, R.string.settings_buy_banana_title, R.string.settings_buy_banana_desc, this::onClickBuyBanana)
                .addItem(R.drawable.ic_telegram, R.string.settings_telegram_ch_title, R.string.settings_telegram_ch_desc, this::onClickTelegram)
                .addItem(R.drawable.ic_about, R.string.settings_about_title, R.string.settings_about_desc, this::onClickAbout);
        List<SettingsItemRow> rows = builder.build();

        mAdapter.addRows(rows);
        getViewState().setAdapter(mAdapter);
    }

    private void onClickChangeWallet(SettingsItemRow.ItemData itemData) {
        getViewState().startChangeWalletDialog(address -> {
            accountStorage.observe()
                    .doOnSubscribe(this::unsubscribeOnDestroy)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(res -> {
                        Timber.d("Loaded balance");
                        AccountItem bip = res.findByCoin(MinterSDK.DEFAULT_COIN);
                        getViewState().setBalance(bip.getBalance());
                    });
            expAddressRepo.observe()
                    .doOnSubscribe(this::unsubscribeOnDestroy)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(res -> {
                        Timber.d("Loaded delegated");
                        if (res.getMeta().additional != null && res.getMeta().additional.delegatedAmount != null) {
                            getViewState().setDelegatedBalance(res.meta.additional.delegatedAmount);
                        } else {
                            getViewState().setDelegatedBalance(BigDecimal.ZERO);
                        }
                    });
            accountStorage.update(true);
            expAddressRepo.update(true);
        });
    }

    private void onClickAbout(SettingsItemRow.ItemData itemData) {
        getViewState().startAbout();
    }

    private void onClickTelegram(SettingsItemRow.ItemData itemData) {
        getViewState().startTelegram();
    }

    private void onClickBuyBanana(SettingsItemRow.ItemData itemData) {
        getViewState().startBuyBanana();
    }

    private void onClickTransactions(SettingsItemRow.ItemData itemData) {
        getViewState().startTransactionsList(secretStorage.getAddresses().get(0).toString());
    }

    private void onClickDonate(SettingsItemRow.ItemData itemData) {
        getViewState().startDonationDialog(
                HtmlCompat.fromHtml(res.getString(R.string.deposit_donate_title)),
                "Mx408fb7d25f40d0361ee370cff812c1fe1fac74a7",
                prefs.getString(PrefKeys.QR_TEAM_BITMAP_PATH, null)
        );
    }

    private void onClickRate(SettingsItemRow.ItemData itemData) {
        getViewState().startRateApp();
    }

    private void onClickReport(SettingsItemRow.ItemData itemData) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "monkeapp@gmail.com", null));
        intent.putExtra(Intent.EXTRA_EMAIL, "monkeapp@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Monke Report");
        intent.putExtra(Intent.EXTRA_TEXT, String.format("\n\n\n\nAndroid %d\nMonke version: %s\n", Build.VERSION.SDK_INT, BuildConfig.VERSION_NAME));

        getViewState().startIntent(Intent.createChooser(intent, "Send Report"));
    }

    private void onClickBackup(SettingsItemRow.ItemData itemData) {
        getViewState().startBackup(REQUEST_AUTH);
    }

    private void onDayNightSwitch(CompoundButton compoundButton, boolean isChecked) {
        prefs.edit().putBoolean(PrefKeys.DAY_NIGHT_THEME, isChecked).apply();
        getViewState().restartApplication();
    }
}
