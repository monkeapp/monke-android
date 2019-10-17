package io.monke.app.tx;

import com.annimon.stream.Optional;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.inject.Inject;

import io.monke.app.BuildConfig;
import io.monke.app.internal.common.Lazy;
import io.monke.app.internal.data.data.CachedRepository;
import io.monke.app.storage.AccountItem;
import io.monke.app.storage.AccountStorage;
import io.monke.app.storage.AddressAccount;
import io.monke.app.storage.SecretData;
import io.monke.app.storage.SecretStorage;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import network.minter.blockchain.models.BCResult;
import network.minter.blockchain.models.TransactionCommissionValue;
import network.minter.blockchain.models.TransactionSendResult;
import network.minter.blockchain.models.operational.OperationInvalidDataException;
import network.minter.blockchain.models.operational.Transaction;
import network.minter.blockchain.models.operational.TransactionSign;
import network.minter.core.MinterSDK;
import network.minter.core.crypto.MinterAddress;
import network.minter.explorer.models.GateResult;
import network.minter.explorer.repo.GateEstimateRepository;
import network.minter.explorer.repo.GateGasRepository;
import network.minter.explorer.repo.GateTransactionRepository;
import timber.log.Timber;

import static com.google.common.base.MoreObjects.firstNonNull;
import static io.monke.app.apis.reactive.ReactiveGate.rxGate;
import static io.monke.app.apis.reactive.ReactiveGate.toGateError;
import static io.monke.app.internal.helpers.MathHelper.bdGTE;
import static io.monke.app.internal.helpers.MathHelper.bdLT;
import static io.monke.app.internal.helpers.MathHelper.bdMax;
import static io.monke.app.internal.helpers.MathHelper.bdMin;

public class TxSendHandler extends TxHandler {
    @Inject SecretStorage secretStorage;
    private BigDecimal mAmount = BigDecimal.ZERO;
    private String mCoin = MinterSDK.DEFAULT_COIN;
    private MinterAddress mRecipient;
    private AccountItem mFromAccount;
    private BigInteger mGasPrice = new BigInteger("1");
    private String mGasCoin = MinterSDK.DEFAULT_COIN;

    @Inject
    public TxSendHandler(GateEstimateRepository estimateRepo, GateGasRepository gasRepo, GateTransactionRepository txRepo, CachedRepository<AddressAccount, AccountStorage> accountStorage) {
        super(estimateRepo, gasRepo, txRepo, accountStorage);
    }

    public BigDecimal getAmount() {
        return mAmount;
    }

    public void setAmount(BigDecimal amount) {
        if (amount == null) {
            setAmount(BigDecimal.ZERO);
            return;
        }
        mAmount = amount;
        Timber.d("Set amount to send: %s", mAmount.toPlainString());
    }

    @Override
    public Lazy<AccountItem> getAccount() {
        return () -> mFromAccount;
    }

    public void setAccount(AccountItem fromAccount) {
        mFromAccount = fromAccount;
    }

    public String getCoin() {
        return mCoin;
    }

    public void setCoin(String coin) {
        mCoin = firstNonNull(coin, MinterSDK.DEFAULT_COIN);
    }

    public MinterAddress getRecipient() {
        return mRecipient;
    }

    public void setRecipient(MinterAddress address) {
        mRecipient = address;
    }

    public Observable<GateResult<TransactionSendResult>> send() {
        final Optional<AccountItem> bananaAccount = findAccountByCoin(BuildConfig.BANANA_COIN);

        // if no banana account or is zero balance - error, not enough balance
        if (!bananaAccount.isPresent() || bananaAccount.get().getBalance().equals(BigDecimal.ZERO)) {
            GateResult<TransactionSendResult> error = new GateResult<>();
            error.error = new GateResult.ErrorResult();
            error.error.code = BCResult.ResultCode.WrongGasCoin.getValue();
            error.error.coin = BuildConfig.BANANA_COIN;
            error.error.message = "Insufficient funds in " + BuildConfig.BANANA_COIN;
            return Observable.just(GateResult.copyError(error));
        }

        Observable<GateResult<TransactionCommissionValue>> exchangeResolver;

        Timber.d("Not enough balance in %s to pay fee, using %s", MinterSDK.DEFAULT_COIN, mFromAccount.getCoin());
        mGasCoin = bananaAccount.get().getCoin();
        // otherwise getting
        Timber.tag("TX Send").d("Resolving custom coin commission %s", mFromAccount.getCoin());
        // resolving fee currency for custom currency
        // creating tx
        try {
            final Transaction preTx = new Transaction.Builder(new BigInteger("1"))
                    .setGasCoin(mGasCoin)
                    .setGasPrice(mGasPrice)
                    .sendCoin()
                    .setCoin(mFromAccount.coin)
                    .setTo(getRecipient())
                    .setValue(getAmount())
                    .build();

            final SecretData preData = secretStorage.getSecret(mFromAccount.address);
            final TransactionSign preSign = preTx.signSingle(preData.getPrivateKey());

            exchangeResolver = rxGate(getEstimateRepo().getTransactionCommission(preSign)).onErrorResumeNext(toGateError());
        } catch (OperationInvalidDataException e) {
            GateResult<TransactionSendResult> error = new GateResult<>();
            error.error = new GateResult.ErrorResult();
            error.error.message = e.getMessage();
            return Observable.just(error);
        }


        // creating preparation result to send transaction
        return Observable.combineLatest(
                exchangeResolver,

                getTxInitData(getAccount().get().getAddress()),
                (txCommissionValue, data) -> {

                    // if some request failed, returning error result
                    if (!txCommissionValue.isOk()) {
                        return new TxInitData(GateResult.copyError(txCommissionValue));
                    }

                    Timber.d("TxInitData: tx commission: %s", txCommissionValue.result.getValue());

                    data.commission = txCommissionValue.result.getValue();
                    Timber.tag("TX Send").d("Resolved: coin %s commission=%s; nonce=%s; min-gas: %s", mFromAccount.getCoin(), data.commission, data.nonce, data.gas);

                    // creating preparation data
                    return data;
                })
                .switchMap((Function<TxInitData, ObservableSource<GateResult<TransactionSendResult>>>) cntRes -> {
                    // if in previous request we've got error, returning it
                    if (!cntRes.isSuccess()) {
                        return Observable.just(GateResult.copyError(cntRes.errorResult));
                    }

                    final BigDecimal amountToSend;

                    // if after commission calculation, we see, banana balance less than commission - error
                    if (bdLT(bananaAccount.get().getBalance(), cntRes.commission)) {
                        GateResult<TransactionSendResult> error = new GateResult<>();
                        error.error = new GateResult.ErrorResult();
                        error.error.message = "Insufficient funds in " + BuildConfig.BANANA_COIN;
                        return Observable.just(error);
                    }

                    amountToSend = mAmount;

                    Timber.tag("TX Send").d("Send data: gasCoin=%s, coin=%s, to=%s, from=%s, amount=%s",
                            mGasCoin,
                            mFromAccount.getCoin(),
                            getRecipient().toString(),
                            mFromAccount.getAddress().toString(),
                            amountToSend
                    );
                    // creating tx
                    final Transaction tx;
                    Transaction.Builder builder = new Transaction.Builder(cntRes.nonce)
                            .setGasCoin(mGasCoin)
                            .setGasPrice(mGasPrice);

                    tx = builder
                            .sendCoin()
                            .setCoin(mFromAccount.coin)
                            .setTo(getRecipient())
                            .setValue(amountToSend)
                            .build();

                    final SecretData data = secretStorage.getSecret(mFromAccount.address);
                    final TransactionSign sign = tx.signSingle(data.getPrivateKey());
                    data.cleanup();

                    return rxGate(getTxRepo().sendTransaction(sign))
                            .onErrorResumeNext(toGateError());

                });
    }

    public Observable<GateResult<TransactionSendResult>> buyBananaIfNecessary(BigDecimal amount, BigDecimal bananaBalance) {
        if (bdGTE(bananaBalance, MIN_BANANA_ACC) || getAccount().get().getCoin().equals(BuildConfig.BANANA_COIN)) {
            Timber.d("Buy banana is unnecessary");
            return Observable.empty();
        }


        BigDecimal toSell = bdMin(
                amount.multiply(new BigDecimal("0.01")),
                bdMax(BigDecimal.ZERO, MIN_BANANA_ACC.subtract(bananaBalance))
        );

        if (toSell.equals(BigDecimal.ZERO)) {
            Timber.w("Nothing to buy");
            return Observable.empty();
        }

        Timber.d("To sell BIPs to buy BANANA: %s", (toSell.toPlainString()));

        return getTxInitData(getAccount().get().getAddress())
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .switchMap(new Function<TxInitData, ObservableSource<GateResult<TransactionSendResult>>>() {
                    @Override
                    public ObservableSource<GateResult<TransactionSendResult>> apply(TxInitData txInitData) throws Exception {
                        if (!txInitData.isSuccess()) {
                            return Observable.just(GateResult.copyError(txInitData.errorResult));
                        }

                        // creating tx
                        final Transaction tx = new Transaction.Builder(txInitData.nonce)
                                .setGasCoin(mGasCoin)
                                .setGasPrice(txInitData.gas)
                                .sellCoin()
                                .setCoinToBuy(BuildConfig.BANANA_COIN)
                                .setCoinToSell(mFromAccount.getCoin())
                                .setValueToSell(toSell)
                                .setMinValueToBuy("0")
                                .build();

                        final SecretData data = secretStorage.getSecret(mFromAccount.address);
                        final TransactionSign sign = tx.signSingle(data.getPrivateKey());
                        data.cleanup();

                        return rxGate(getTxRepo().sendTransaction(sign))
                                .onErrorResumeNext(toGateError());
                    }
                });
    }
}
