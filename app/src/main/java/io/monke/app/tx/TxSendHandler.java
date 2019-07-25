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
import network.minter.blockchain.models.operational.OperationType;
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
import static io.monke.app.apis.reactive.ReactiveGate.createGateErrorPlain;
import static io.monke.app.apis.reactive.ReactiveGate.rxGate;
import static io.monke.app.apis.reactive.ReactiveGate.toGateError;
import static io.monke.app.internal.helpers.MathHelper.bdGTE;
import static io.monke.app.internal.helpers.MathHelper.bdHuman;
import static io.monke.app.internal.helpers.MathHelper.bdLT;
import static io.monke.app.internal.helpers.MathHelper.bdLTE;
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
    }

    @Override
    public Lazy<AccountItem> getAccount() {
        return () -> mFromAccount;
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
        Optional<AccountItem> mntAccount = findAccountByCoin(MinterSDK.DEFAULT_COIN);
        Optional<AccountItem> sendAccount = findAccountByCoin(mFromAccount.getCoin());

        final boolean enoughBaseCoinForCommission = bdGTE(mntAccount.get().getBalance(), OperationType.SendCoin.getFee());

        // default coin for pay fee - MNT (base coin)
        final GateResult<TransactionCommissionValue> val = new GateResult<>();
        val.result = new TransactionCommissionValue();
        val.result.value = OperationType.SendCoin.getFee().multiply(Transaction.VALUE_MUL_DEC).toBigInteger();

        Observable<GateResult<TransactionCommissionValue>> exchangeResolver = Observable.just(val);

        // if enough balance on MNT account, set gas coin MNT (BIP)
        if (enoughBaseCoinForCommission) {
            Timber.d("Enough balance in %s to pay fee", MinterSDK.DEFAULT_COIN);
            Timber.tag("TX Send").d("Resolving base coin commission %s", MinterSDK.DEFAULT_COIN);
            mGasCoin = mntAccount.get().getCoin();
        }
        // if sending account is not MNT (BIP), set gas coin CUSTOM
        else if (!sendAccount.get().getCoin().equals(MinterSDK.DEFAULT_COIN)) {
            Timber.d("Not enough balance in %s to pay fee, using %s", MinterSDK.DEFAULT_COIN, mFromAccount.getCoin());
            mGasCoin = sendAccount.get().getCoin();
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
                Timber.w(e);
                final GateResult<TransactionCommissionValue> commissionValue = new GateResult<>();
                val.result.value = OperationType.SendCoin.getFee().multiply(Transaction.VALUE_MUL_DEC).toBigInteger();
                exchangeResolver = Observable.just(commissionValue);
            }
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

                    // don't calc fee if enough balance in base coin and we are sending not a base coin (MNT or BIP)
                    if (enoughBaseCoinForCommission && !mFromAccount.getCoin().equals(MinterSDK.DEFAULT_COIN)) {
                        cntRes.commission = new BigDecimal(0);
                    }

                    // if balance enough to send required sum + fee, do nothing
                    if (bdLTE(mAmount.add(cntRes.commission), mFromAccount.getBalance())) {
                        Timber.tag("TX Send").d("Don't change sending amount - balance enough to send");
                        amountToSend = mAmount;
                    }
                    // if balance not enough to send required sum + fee - subtracting fee from sending sum ("use max" for example)
                    else {
                        amountToSend = mAmount.subtract(cntRes.commission);
                        Timber.tag("TX Send").d("Subtracting sending amount (-%s): balance not enough to send", cntRes.commission);
                    }


                    // if after subtracting fee from sending sum has become less than account balance at all, returning error with message "insufficient funds"
                    if (bdLT(amountToSend, 0)) {
                        GateResult<TransactionSendResult> errorRes;
                        final BigDecimal balanceMustBe = cntRes.commission.add(mAmount);
                        if (bdLT(mAmount, mFromAccount.getBalance())) {
                            final BigDecimal notEnough = cntRes.commission.subtract(mFromAccount.getBalance().subtract(mAmount));
                            Timber.d("Amount: %s, fromAcc: %s, diff: %s", bdHuman(mAmount), bdHuman(mFromAccount.getBalance()), bdHuman(notEnough));
                            errorRes = createGateErrorPlain(
                                    String.format("Insufficient funds: not enough %s %s, wanted: %s %s", bdHuman(notEnough), mFromAccount.getCoin(), bdHuman(balanceMustBe), mFromAccount.getCoin()),
                                    BCResult.ResultCode.InsufficientFunds.getValue(),
                                    400
                            );
                        } else {
                            Timber.d("Amount: %s, fromAcc: %s, diff: %s", bdHuman(mAmount), bdHuman(mFromAccount.getBalance()), bdHuman(balanceMustBe));
                            errorRes = createGateErrorPlain(
                                    String.format("Insufficient funds: wanted %s %s", bdHuman(balanceMustBe), mFromAccount.getCoin()),
                                    BCResult.ResultCode.InsufficientFunds.getValue(),
                                    400
                            );
                        }

                        return Observable.just(errorRes);
                    }

                    Timber.tag("TX Send").d("Send data: gasCoin=%s, coin=%s, to=%s, from=%s, amount=%s",
                            mFromAccount.getCoin(),
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
        BigDecimal toSell = bdMin(
                amount.multiply(new BigDecimal("0.01")),
                bdMax(BigDecimal.ZERO, BigDecimal.ONE.subtract(bananaBalance))
        );

        return getTxInitData(getAccount().get().getAddress())
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .switchMap(new Function<TxInitData, ObservableSource<GateResult<TransactionSendResult>>>() {
                    @Override
                    public ObservableSource<GateResult<TransactionSendResult>> apply(TxInitData txInitData) throws Exception {
                        if(!txInitData.isSuccess()) {
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
                                .setMinValueToBuy(0)
                                .build();

                        final SecretData data = secretStorage.getSecret(mFromAccount.address);
                        final TransactionSign sign = tx.signSingle(data.getPrivateKey());
                        data.cleanup();

                        return rxGate(getTxRepo().sendTransaction(sign))
                                .onErrorResumeNext(toGateError());
                    }
                });


//        amount.multiply(new BigDecimal("0.01")).min(BigDecimal.ZERO.max())
//        Math.min(amount*0.01, max(0, 1 - bananaBalance))
    }

    public void setAccount(AccountItem fromAccount) {
        mFromAccount = fromAccount;
    }
}
