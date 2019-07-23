package io.monke.app.ime;

import android.content.Context;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.annimon.stream.Stream;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.preference.PreferenceManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import io.monke.app.BuildConfig;
import io.monke.app.R;
import io.monke.app.apis.explorer.CachedExplorerAddressRepository;
import io.monke.app.ime.screens.SendScreen;
import io.monke.app.internal.PrefKeys;
import io.monke.app.internal.common.Lazy;
import io.monke.app.internal.data.data.CachedRepository;
import io.monke.app.internal.helpers.ViewHelper;
import io.monke.app.services.ServiceConnector;
import io.monke.app.storage.AccountItem;
import io.monke.app.storage.AccountStorage;
import io.monke.app.storage.AddressAccount;
import io.monke.app.storage.SecretStorage;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import network.minter.core.MinterSDK;
import network.minter.core.crypto.MinterAddress;
import network.minter.explorer.models.DelegationInfo;
import network.minter.explorer.models.ExpResult;
import timber.log.Timber;

import static io.monke.app.internal.helpers.MathHelper.bdGTE;
import static io.monke.app.internal.helpers.MathHelper.bdHuman;

public class MonkeKeyboard extends InputMethodService {

    @BindView(R.id.wallet_address) TextView walletAddress;
    @BindView(R.id.balance) TextView balance;
    @BindView(R.id.balance_delegated) TextView balanceDelegated;
    @BindView(R.id.bip_icon) View home;
    @BindView(R.id.bip_progress) View progress;
    @BindView(R.id.hide_keyboard) View buttonHideKeyboard;
    @BindView(R.id.btn_close) View buttonClose;
    @BindView(R.id.switch_keyboard) View buttonSwitchKeyboard;
    @BindView(R.id.error_container) View errorContainer;
    @BindView(R.id.error_text) TextView errorText;

    @Inject SecretStorage secretStorage;
    @Inject CachedRepository<AddressAccount, AccountStorage> accountStorage;
    @Inject
    CachedRepository<ExpResult<List<DelegationInfo>>, CachedExplorerAddressRepository> expAddressRepo;
    @Inject SendScreen screenSend;
    private ConstraintLayout mKeyboard;
    private AccountItem mAccount;
    private AccountItem mBananaAccount;
    private MinterAddress mAddress;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private List<OnUpdateAccountListener> mOnUpdateAccountListeners = new ArrayList<>(3);
    private List<OnUpdateDelegatedListener> mOnUpdateDelegatedListener = new ArrayList<>(3);

    public void showCloseButton(boolean show) {
        showCloseButton(show, null);
    }

    public void showCloseButton(boolean show, View.OnClickListener listener) {
        ViewHelper.switchView(buttonClose, buttonHideKeyboard, show);
        buttonClose.setOnClickListener(show ? listener : null);
    }

    @Override
    public void onCreate() {
        final SharedPreferences defPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        setTheme(defPrefs.getBoolean(PrefKeys.DAY_NIGHT_THEME, false) ? R.style.KB_Dark : R.style.KB_Light);
        super.onCreate();
        AndroidInjection.inject(this);

        ServiceConnector.bind(getApplicationContext());
        ServiceConnector.onConnected()
                .subscribe(res -> res.setOnMessageListener((message, channel, address) -> {
                    Timber.d("WS ON MESSAGE[%s]: %s", channel, message);
                    accountStorage.update(true);
                }));
    }

    public boolean hasEnoughBanana() {
        return Stream.of(accountStorage.getData().getAccountsItems())
                .filter(item -> item.coin.toLowerCase().equals(BuildConfig.BANANA_COIN.toLowerCase()))
                .filter(item -> bdGTE(item.getBalance(), BigDecimal.ONE))
                .count() > 0;
    }

    public void setError(CharSequence error) {
        if(error != null) {
            showProgress(false);
        }
        errorText.setText(error);
//        errorText.setVisibility(error != null ? View.VISIBLE : View.GONE);
    }

    public Lazy<AccountItem> getAccount() {
        return () -> mAccount;
    }

    public Lazy<AccountItem> getBananaAccount() {
        return () -> mBananaAccount;
    }

    public MinterAddress getAddress() {
        return mAddress;
    }

    public void addOnUpdateAccountListener(OnUpdateAccountListener listener) {
        mOnUpdateAccountListeners.add(listener);
    }

    public void updateBalance(boolean force) {
        accountStorage.update(force);
        expAddressRepo.update(force);
    }

    @Override
    public View onCreateInputView() {
        mKeyboard = (ConstraintLayout) getLayoutInflater().inflate(R.layout.keyboard_view, null);

        ButterKnife.bind(this, mKeyboard);
        screenSend.init(this, mKeyboard);

        mAddress = secretStorage.getAddresses().get(0);

        setBalance(BigDecimal.ZERO);
        setDelegatedBalance(BigDecimal.ZERO);

        home.setOnClickListener(v -> {
            showProgress(true);
            accountStorage.update(true);
            expAddressRepo.update(true);
        });

        mAccount = accountStorage.getEntity().getData().findByCoin(MinterSDK.DEFAULT_COIN, mAddress);
        mBananaAccount = accountStorage.getEntity().getData().findByCoin(BuildConfig.BANANA_COIN, mAddress);


        walletAddress.setText(mAddress.toShortString());
        walletAddress.setOnClickListener(this::onClickWallet);
        accountStorage.update();
        accountStorage.observe()
                .doOnSubscribe(d -> mDisposables.add(d))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(res -> {
                    Timber.d("Loaded balance");
                    showProgress(false);
                    mAccount = res.findByCoin(MinterSDK.DEFAULT_COIN, mAddress);
                    mBananaAccount = res.findByCoin(BuildConfig.BANANA_COIN, mAddress);
                    setBalance(res.getTotalBalance());
                    Stream.of(mOnUpdateAccountListeners).forEach(item -> item.onUpdate(res));
                });


        expAddressRepo.update();
        expAddressRepo.observe()
                .doOnSubscribe(d -> mDisposables.add(d))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doFinally(() -> showProgress(false))
                .subscribe(res -> {
                    Timber.d("Loaded delegated");
                    showProgress(false);
                    if (res.getMeta().additional != null && res.getMeta().additional.delegatedAmount != null) {
                        setDelegatedBalance(res.meta.additional.delegatedAmount);
                    }

                    Stream.of(mOnUpdateDelegatedListener).forEach(item -> item.onUpdate(res));
                });


        buttonHideKeyboard.setOnClickListener(v -> {
            requestHideSelf(InputMethodManager.HIDE_NOT_ALWAYS);
        });
        buttonSwitchKeyboard.setOnClickListener(v -> {
            InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (mgr != null) {
                mgr.showInputMethodPicker();
            }
        });

        return mKeyboard;
    }

    public void showProgress(boolean show) {
        home.setAlpha(show ? 0.5f : 1.0f);
        progress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDisposables != null) {
            mDisposables.dispose();
        }
        screenSend.destroy();
        ServiceConnector.release(getApplicationContext());
        Timber.d("OnDestroy KB");
    }

    public void addOnUpdateDelegatedListener(OnUpdateDelegatedListener listener) {
        mOnUpdateDelegatedListener.add(listener);
    }

    private void setBalance(BigDecimal value) {
        balance.setText(bdHuman(value));

    }

    private void setDelegatedBalance(BigDecimal delegatedBalance) {
        balanceDelegated.setText(bdHuman(delegatedBalance));
    }

    private void onClickWallet(View view) {
        // start app
        InputConnection inputConnection = getCurrentInputConnection();
        inputConnection.commitText(mAccount.getAddress().toString(), 0);
    }

    public interface OnUpdateAccountListener {
        void onUpdate(AddressAccount account);
    }

    public interface OnUpdateDelegatedListener {
        void onUpdate(ExpResult<List<DelegationInfo>> result);
    }
}
