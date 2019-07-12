package io.monke.app.apis.explorer;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import io.monke.app.internal.data.data.CachedEntity;
import io.monke.app.internal.storage.KVStorage;
import io.monke.app.storage.SecretStorage;
import io.reactivex.Observable;
import network.minter.core.internal.api.ApiService;
import network.minter.explorer.models.DelegationInfo;
import network.minter.explorer.models.ExpResult;
import network.minter.explorer.repo.ExplorerAddressRepository;

import static io.monke.app.apis.reactive.ReactiveExplorer.rxExp;
import static io.monke.app.apis.reactive.ReactiveExplorer.toExpError;

public class CachedExplorerAddressRepository extends ExplorerAddressRepository implements CachedEntity<ExpResult<List<DelegationInfo>>> {
    private final static String KEY_DELEGATIONS = "cached_explorer_address_repository_delegations";
    private final KVStorage mStorage;
    private final SecretStorage mSecretStorage;

    public CachedExplorerAddressRepository(KVStorage storage, SecretStorage secretStorage, @Nonnull ApiService.Builder apiBuilder) {
        super(apiBuilder);
        mSecretStorage = secretStorage;
        mStorage = storage;
    }

    @Override
    public ExpResult<List<DelegationInfo>> initialData() {
        ExpResult<List<DelegationInfo>> dummy = new ExpResult<>();
        dummy.result = Collections.emptyList();
        return mStorage.get(KEY_DELEGATIONS, dummy);
    }

    @Override
    public Observable<ExpResult<List<DelegationInfo>>> getUpdatableData() {
        return rxExp(getDelegations(mSecretStorage.getAddresses().get(0), 1))
                .onErrorResumeNext(toExpError())
                .map(res -> {
                    if (res.result != null) {
                        return res;
                    }

                    ExpResult<List<DelegationInfo>> dummy = new ExpResult<>();
                    dummy.result = Collections.emptyList();

                    return dummy;
                });
    }

    @Override
    public void onAfterUpdate(ExpResult<List<DelegationInfo>> result) {
        mStorage.put(KEY_DELEGATIONS, result);
    }

    @Override
    public void onClear() {
        mStorage.delete(KEY_DELEGATIONS);
    }
}
