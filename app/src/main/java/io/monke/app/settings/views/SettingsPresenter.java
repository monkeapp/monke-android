package io.monke.app.settings.views;

import android.content.SharedPreferences;
import android.widget.CompoundButton;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import io.monke.app.R;
import io.monke.app.apis.explorer.CachedExplorerAddressRepository;
import io.monke.app.internal.Monke;
import io.monke.app.internal.PrefKeys;
import io.monke.app.internal.data.data.CachedRepository;
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

@InjectViewState
public class SettingsPresenter extends MvpBasePresenter<SettingsView> {

    @Inject SecretStorage secretStorage;
    @Inject CachedRepository<AddressAccount, AccountStorage> accountStorage;
    @Inject
    CachedRepository<ExpResult<List<DelegationInfo>>, CachedExplorerAddressRepository> expAddressRepo;

    @Inject SharedPreferences prefs;

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
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        SettingsItemRow.ItemData dayNightOpt = new SettingsItemRow.ItemData(0, "Use Night theme", null, null)
                .setSwitchListener(this::onDayNightSwitch)
                .setChecked(() -> prefs.getBoolean(PrefKeys.DAY_NIGHT_THEME, false));

        List<SettingsItemRow> rows = new SettingsItemRow.Builder(Monke.app().context())
                .addItem(dayNightOpt)
                .addItem(R.string.settings_backup_mnemonic, this::onClickBackup)
                .addItem(R.string.settings_report_problem, this::onClickReport)
                .addItem(R.string.settings_rate_app, this::onClickRate)
                .addItem(R.drawable.ic_make_donation, R.string.settings_make_donation_title, R.string.settings_make_donation_desc, this::onClickDonate)
                .addItem(R.drawable.ic_buy_banana, R.string.settings_buy_banana_title, R.string.settings_buy_banana_desc, this::onClickBuyBanana)
                .addItem(R.drawable.ic_telegram, R.string.settings_telegram_ch_title, R.string.settings_telegram_ch_desc, this::onClickTelegram)
                .addItem(R.drawable.ic_about, R.string.settings_about_title, R.string.settings_about_desc, this::onClickAbout)
                .build();

        mAdapter.addRows(rows);
        getViewState().setAdapter(mAdapter);
    }

    private void onClickAbout(SettingsItemRow.ItemData itemData) {
        getViewState().startAbout();
    }

    private void onClickTelegram(SettingsItemRow.ItemData itemData) {
        getViewState().startTelegram();
    }

    private void onClickBuyBanana(SettingsItemRow.ItemData itemData) {

    }

    private void onClickDonate(SettingsItemRow.ItemData itemData) {

    }

    private void onClickRate(SettingsItemRow.ItemData itemData) {
        getViewState().startRateApp();
    }

    private void onClickReport(SettingsItemRow.ItemData itemData) {

    }

    private void onClickBackup(SettingsItemRow.ItemData itemData) {
        getViewState().startBackup();
    }

    private void onDayNightSwitch(CompoundButton compoundButton, boolean isChecked) {
        prefs.edit().putBoolean(PrefKeys.DAY_NIGHT_THEME, isChecked).apply();
        getViewState().restartApplication();
    }
}
