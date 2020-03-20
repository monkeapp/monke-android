package io.monke.app.setup.views;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.EditText;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.monke.app.BuildConfig;
import io.monke.app.R;
import io.monke.app.internal.Monke;
import io.monke.app.internal.data.data.CachedRepository;
import io.monke.app.internal.dialogs.WalletConfirmDialog;
import io.monke.app.internal.helpers.MathHelper;
import io.monke.app.internal.mvp.MvpBasePresenter;
import io.monke.app.services.ServiceConnector;
import io.monke.app.setup.contract.BuyBananaView;
import io.monke.app.setup.ui.WalletAccountSelectorDialog;
import io.monke.app.storage.AccountItem;
import io.monke.app.storage.AccountStorage;
import io.monke.app.storage.AddressAccount;
import io.monke.app.storage.SecretData;
import io.monke.app.storage.SecretStorage;
import io.monke.app.tx.TxHandler;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import moxy.InjectViewState;
import network.minter.blockchain.models.TransactionSendResult;
import network.minter.blockchain.models.operational.OperationType;
import network.minter.blockchain.models.operational.Transaction;
import network.minter.blockchain.models.operational.TransactionSign;
import network.minter.core.MinterSDK;
import network.minter.core.crypto.MinterHash;
import network.minter.explorer.MinterExplorerApi;
import network.minter.explorer.models.GateResult;
import network.minter.explorer.repo.ExplorerCoinsRepository;
import network.minter.explorer.repo.GateEstimateRepository;
import network.minter.explorer.repo.GateGasRepository;
import network.minter.explorer.repo.GateTransactionRepository;
import timber.log.Timber;

import static io.monke.app.apis.reactive.ReactiveGate.rxGate;
import static io.monke.app.apis.reactive.ReactiveGate.toGateError;
import static io.monke.app.internal.helpers.MathHelper.bdHuman;
import static io.monke.app.internal.helpers.MathHelper.bdNull;

@InjectViewState
public class BuyBananaPresenter extends MvpBasePresenter<BuyBananaView> {
    @Inject SecretStorage secretStorage;
    @Inject CachedRepository<AddressAccount, AccountStorage> accountStorage;
    @Inject ExplorerCoinsRepository explorerCoinsRepo;
    @Inject GateEstimateRepository estimateRepository;
    @Inject GateGasRepository gasRepo;
    @Inject GateTransactionRepository gateTxRepo;
    private BigDecimal mSpendAmount = BigDecimal.ZERO;
    private String mGasCoin = BuildConfig.BANANA_COIN;
    private AccountItem mAccount;
    private BehaviorSubject<Boolean> mCalculatorSubject;
    private TxHandler mTxHandler;
    private BigDecimal mEstimate = BigDecimal.ZERO;

    @Inject
    public BuyBananaPresenter() {
    }

    @Override
    public void attachView(BuyBananaView view) {
        super.attachView(view);
        getViewState().setFee(String.format("Fee: %s %s", bdHuman(OperationType.SellCoin.getFee()), MinterSDK.DEFAULT_COIN));
        ServiceConnector.bind(Monke.app().context());
        ServiceConnector.onConnected()
                .subscribe(res -> res.setOnMessageListener((message, channel, address) -> {
                    Timber.d("WS ON MESSAGE[%s]: %s", channel, message);
                    accountStorage.update(true);
                }));

        accountStorage.update();
        accountStorage.observe()
                .doOnSubscribe(this::unsubscribeOnDestroy)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(res -> {
                    Timber.d("Loaded balance");
                    AccountItem bip = res.findByCoin(MinterSDK.DEFAULT_COIN);
                    mAccount = bip;
                    mTxHandler.init(() -> mAccount);
                    getViewState().setCoinIcon(mAccount.getAvatar());
                    getViewState().setCoin(mAccount.getCoin());
                    getViewState().setOnCoinClickListener(v -> {
                        getViewState().startDialog(ctx -> new WalletAccountSelectorDialog.Builder(ctx, R.string.dialog_title_select_account)
                                .setItems(res.getAccountsItems())
                                .setOnClickListener(BuyBananaPresenter.this::onAccountSelect)
                                .create());
                    });
                    getViewState().setBalance(bip.getBalance(), bip.getCoin());
                });

        getViewState().setFormValidationListener(valid -> {
            getViewState().setSubmitEnabled(valid && checkZero(mSpendAmount));
        });
        getViewState().setTextChangedListener(this::onInputChanged);
        getViewState().setOnSubmit(this::onSubmit);
    }

    @Override
    public void detachView(BuyBananaView view) {
        super.detachView(view);
        ServiceConnector.release(Monke.app().context());
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().setCoin(MinterSDK.DEFAULT_COIN);

        mTxHandler = new TxHandler(estimateRepository, gasRepo, gateTxRepo, accountStorage);
        mCalculatorSubject = BehaviorSubject.create();
        unsubscribeOnDestroy(mCalculatorSubject
                .toFlowable(BackpressureStrategy.LATEST)
                .debounce(200, TimeUnit.MILLISECONDS)
                .subscribe(this::onCalculateRequest));
    }

    private void onSubmit(View view) {
        getViewState().showProgress(true);

        mTxHandler.getTxInitData(secretStorage.getAddresses().get(0))
                .switchMap(new Function<TxHandler.TxInitData, Observable<GateResult<TransactionSendResult>>>() {
                    @Override
                    public Observable<GateResult<TransactionSendResult>> apply(TxHandler.TxInitData txInitData) throws Exception {
                        BigDecimal minValue = mEstimate.subtract(mEstimate.multiply(new BigDecimal("0.1")));
                        Transaction tx = new Transaction.Builder(txInitData.nonce)
                                .setGasCoin(mGasCoin)
                                .setGasPrice(txInitData.gas)
                                .setBlockchainId(network.minter.blockchain.BuildConfig.BLOCKCHAIN_ID)
                                .sellCoin()
                                .setCoinToBuy(BuildConfig.BANANA_COIN)
                                .setCoinToSell(mAccount.getCoin())
                                .setValueToSell(mSpendAmount)
                                .setMinValueToBuy(minValue)
                                .build();

                        final SecretData secret = secretStorage.getSecret(secretStorage.getAddresses().get(0));
                        TransactionSign sign = tx.signSingle(secret.getPrivateKey());
                        return rxGate(gateTxRepo.sendTransaction(sign))
                                .onErrorResumeNext(toGateError());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnComplete(() -> getViewState().showProgress(false))
                .subscribe(this::onSendSuccess, this::onSendFailed);
    }

    private void onSendFailed(Throwable t) {
        getViewState().setError(t.getMessage());
    }

    private void onSendError(GateResult<?> errorResult) {
        getViewState().setError(errorResult.getMessage());
    }

    private void onSendSuccess(GateResult<TransactionSendResult> result) {
        Timber.d("Execute Success: %b", result.isOk());
        if (!result.isOk()) {
            onSendError(result);
            return;
        }

        accountStorage.update(true);

        getViewState().startDialog(ctx ->
                new WalletConfirmDialog.Builder(ctx, "Success")
                        .setText("You've success bought bananas!")
                        .setPositiveAction(R.string.btn_view_tx, (d, w) -> openTx(result.result.txHash))
                        .setNegativeAction(R.string.btn_close, (d, w) -> getViewState().finishSuccess())
                        .create()
        );
    }

    private void openTx(MinterHash txHash) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(MinterExplorerApi.newFrontUrl() + "transactions/" + txHash));
        getViewState().startIntent(intent);
    }

    private void onAccountSelect(AccountItem accountItem) {
        mGasCoin = accountItem.getCoin();
        mAccount = accountItem;
        mTxHandler.init(() -> mAccount);
        getViewState().setCoin(mAccount.getCoin());
        getViewState().setCoinIcon(mAccount.getAvatar());
        getViewState().setBalance(mAccount.getBalance(), mAccount.getCoin());
        mCalculatorSubject.onNext(true);
    }

    private void onCalculateRequest(Boolean getting) {
        rxGate(estimateRepository.getCoinExchangeCurrencyToSell(mAccount.getCoin(), mSpendAmount, BuildConfig.BANANA_COIN))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(res -> {
                    if (res.isOk()) {
                        mEstimate = res.result.getAmount();
                        getViewState().setGetAmount(res.result.getAmount());
                    } else {
                        getViewState().setError(res.error.getMessage());
                    }
                });
    }

    private void onInputChanged(EditText editText, boolean valid) {
        String text = editText.getText().toString();
        switch (editText.getId()) {
            case R.id.input_spend_amount:
                final BigDecimal spendAmount = MathHelper.bigDecimalFromString(text);
                checkZero(spendAmount);
                mSpendAmount = spendAmount;
                mCalculatorSubject.onNext(false);
                break;
        }
    }

    private boolean checkZero(BigDecimal amount) {
        boolean valid = amount == null || !bdNull(amount);
        if (!valid) {
            getViewState().setError("Amount must be greater than 0");
        } else {
            getViewState().setError(null);
        }

        return valid;
    }
}
