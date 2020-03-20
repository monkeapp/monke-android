package io.monke.app.tx;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.monke.app.BuildConfig;
import io.monke.app.internal.common.Lazy;
import io.monke.app.internal.data.data.CachedRepository;
import io.monke.app.storage.AccountItem;
import io.monke.app.storage.AccountStorage;
import io.monke.app.storage.AddressAccount;
import io.reactivex.Observable;
import network.minter.core.crypto.MinterAddress;
import network.minter.explorer.models.GateResult;
import network.minter.explorer.repo.GateEstimateRepository;
import network.minter.explorer.repo.GateGasRepository;
import network.minter.explorer.repo.GateTransactionRepository;

import static io.monke.app.apis.reactive.ReactiveGate.rxGate;

public class TxHandler {

    public static final BigDecimal MIN_BANANA_ACC;

    static {
        //noinspection ConstantConditions
        if (BuildConfig.BANANA_COIN.equals("BANANATEST")) {
            MIN_BANANA_ACC = new BigDecimal("80227");
        } else {
            MIN_BANANA_ACC = BigDecimal.ONE;
        }
    }

    private final GateEstimateRepository mEstimateRepo;
    private final GateGasRepository mGasRepo;
    private final GateTransactionRepository mTxRepo;
    private final CachedRepository<AddressAccount, AccountStorage> mAccountStorage;
    private Lazy<AccountItem> mAccount;

    public TxHandler(GateEstimateRepository estimateRepo, GateGasRepository gasRepo, GateTransactionRepository txRepo, CachedRepository<AddressAccount, AccountStorage> accountStorage) {
        mEstimateRepo = estimateRepo;
        mGasRepo = gasRepo;
        mTxRepo = txRepo;
        mAccountStorage = accountStorage;
    }

    public void init(Lazy<AccountItem> account) {
        mAccount = account;
    }

    public Lazy<AccountItem> getAccount() {
        return mAccount;
    }

    public CachedRepository<AddressAccount, AccountStorage> getAccountStorage() {
        return mAccountStorage;
    }

    public Optional<AccountItem> findAccountByCoin(String coin) {
        return Stream.of(getAccountStorage().getData().getAccountsItems())
                .filter(item -> item.getCoin().equals(coin.toUpperCase()))
                .findFirst();
    }

    public GateGasRepository getGasRepo() {
        return mGasRepo;
    }

    public GateEstimateRepository getEstimateRepo() {
        return mEstimateRepo;
    }

    public GateTransactionRepository getTxRepo() {
        return mTxRepo;
    }

    public Observable<TxInitData> getTxInitData(MinterAddress address) {
        return Observable.combineLatest(
                rxGate(getEstimateRepo().getTransactionCount(address)),
                rxGate(getGasRepo().getMinGas()),
                (txCountGateResult, gasValueGateResult) -> {

                    // if some request failed, returning error result
                    if (!txCountGateResult.isOk()) {
                        return new TxInitData(GateResult.copyError(txCountGateResult));
                    } else if (!gasValueGateResult.isOk()) {
                        return new TxInitData(GateResult.copyError(gasValueGateResult));
                    }

                    return new TxInitData(
                            txCountGateResult.result.count.add(BigInteger.ONE),
                            gasValueGateResult.result.gas

                    );
                }
        );

    }

    public static class TxInitData {
        public BigInteger nonce;
        public BigInteger gas;
        public BigDecimal commission;
        public GateResult<?> errorResult;

        public TxInitData(BigInteger nonce, BigInteger gas) {
            this.nonce = nonce;
            this.gas = gas;
        }

        public TxInitData(GateResult<?> err) {
            errorResult = err;
        }

        boolean isSuccess() {
            return errorResult == null || errorResult.isOk();
        }
    }

}
