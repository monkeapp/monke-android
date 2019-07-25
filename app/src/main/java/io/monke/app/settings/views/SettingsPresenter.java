package io.monke.app.settings.views;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import io.monke.app.apis.explorer.CachedExplorerAddressRepository;
import io.monke.app.internal.data.data.CachedRepository;
import io.monke.app.internal.mvp.MvpBasePresenter;
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

    @Inject
    public SettingsPresenter() {

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
}
