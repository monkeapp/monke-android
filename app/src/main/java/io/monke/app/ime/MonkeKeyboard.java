package io.monke.app.ime;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.List;

import javax.inject.Inject;

import androidx.constraintlayout.widget.ConstraintLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import io.monke.app.R;
import io.monke.app.account.WalletAccountSelectorDialog;
import io.monke.app.apis.explorer.CachedExplorerAddressRepository;
import io.monke.app.internal.data.data.CachedRepository;
import io.monke.app.internal.dialogs.WalletDialog;
import io.monke.app.internal.views.widgets.BipCircleImageView;
import io.monke.app.storage.AccountItem;
import io.monke.app.storage.AccountStorage;
import io.monke.app.storage.SecretStorage;
import io.monke.app.storage.UserAccount;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import network.minter.core.MinterSDK;
import network.minter.core.crypto.MinterAddress;
import network.minter.explorer.models.DelegationInfo;
import network.minter.explorer.models.ExpResult;
import timber.log.Timber;

import static io.monke.app.internal.helpers.MathHelper.bdHuman;

public class MonkeKeyboard extends InputMethodService implements KeyboardView.OnKeyListener {

    @BindView(R.id.input_amount) EditText inputAmount;
    @BindView(R.id.input_address) EditText inputAddress;
    @BindView(R.id.input_coin) EditText inputCoin;
    @BindView(R.id.wallet_address) TextView walletAddress;
    @BindView(R.id.balance) TextView balance;
    @BindView(R.id.balance_delegated) TextView balanceDelegated;
    @BindView(R.id.balance_available) TextView balanceAvailable;
    @BindView(R.id.fragment_container) FrameLayout fragmentContainer;
    @BindView(R.id.keypad_hex) ViewGroup keyboardHex;
    @BindView(R.id.keypad_digits) ViewGroup keyboardNumpad;
    @BindView(R.id.amount_input_container) ViewGroup inputAmountContainer;
    @BindView(R.id.coin_icon) BipCircleImageView coinIcon;
    @Inject SecretStorage secretStorage;
    @Inject CachedRepository<UserAccount, AccountStorage> accountStorage;
    @Inject AccountStorage accounts;
    @Inject
    CachedRepository<ExpResult<List<DelegationInfo>>, CachedExplorerAddressRepository> expAddressRepo;
    private ConstraintLayout mKeyboard;
    private AccountItem mAccount;
    private MinterAddress mAddress;
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private String mLastCoin = MinterSDK.DEFAULT_COIN;
    private WalletDialog mDialog;

    @Override
    public void onCreate() {
        super.onCreate();
        AndroidInjection.inject(this);
    }

    @Override
    public View onCreateInputView() {
        mKeyboard = (ConstraintLayout) getLayoutInflater().inflate(R.layout.keyboard_view, null);
        ButterKnife.bind(this, mKeyboard);
        setBalance(BigDecimal.ZERO);
        setDelegatedBalance(BigDecimal.ZERO);


        mAccount = accounts.getAccount().getFirstAccountItem();
        mAddress = secretStorage.getAddresses().get(0);
        walletAddress.setText(mAddress.toShortString());
        walletAddress.setOnClickListener(this::onClickWallet);
        accountStorage.update();
        accountStorage.observe()
                .doOnSubscribe(d -> mDisposables.add(d))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(res -> {
                    Timber.d("Loaded balance");
                    mAccount = res.getFirstAccountItem();
                    setBalance(res.getTotalBalance());
                });

        expAddressRepo.update();
        expAddressRepo.observe()
                .doOnSubscribe(d -> mDisposables.add(d))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(res -> {
                    Timber.d("Loaded delegated");
                    setDelegatedBalance(res.meta.additional.delegatedAmount);
                });
        inputCoin.setText(mLastCoin);

        inputAmount.setOnFocusChangeListener((view, hasFocus) -> {
            keyboardHex.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        });

        inputAddress.setOnFocusChangeListener((v, hasFocus) -> {
            keyboardNumpad.setVisibility(hasFocus ? View.VISIBLE : View.GONE);
        });

        inputCoin.setOnClickListener(v -> {
            if (mAccount == null) {
                Toast.makeText(getApplicationContext(), "Unable to select coin: internet connection problem", Toast.LENGTH_LONG).show();
                return;
            }
            mDialog = new WalletAccountSelectorDialog.Builder(getApplicationContext())
                    .addItem(mAccount)
                    .setOnClickListener(item -> {
                        inputCoin.setText(item.getCoin().toUpperCase());
                        mLastCoin = item.getCoin().toUpperCase();
                        coinIcon.setImageUrl(item.getAvatar());
                        mDialog.dismiss();
                    })
                    .create();
            mDialog.show();
        });

        return mKeyboard;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
            mDialog = null;
        }
        Timber.d("OnDestroy KB");
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        return false;
    }

    private void setBalance(BigDecimal value) {
        balance.setText(bdHuman(value));
        balanceAvailable.setText(getString(R.string.balance_available, bdHuman(value), MinterSDK.DEFAULT_COIN));
    }

    private void setDelegatedBalance(BigDecimal delegatedBalance) {
        balanceDelegated.setText(bdHuman(delegatedBalance));
    }

    private void onClickWallet(View view) {
        // start app
    }

    private void onClickAmount(View view) {
        Timber.d("OnClickSomething");
        keyboardHex.setVisibility(View.VISIBLE);
    }
}
