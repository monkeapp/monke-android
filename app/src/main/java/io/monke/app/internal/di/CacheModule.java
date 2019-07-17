package io.monke.app.internal.di;


import java.util.List;
import java.util.Set;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import io.monke.app.apis.explorer.CachedExplorerAddressRepository;
import io.monke.app.apis.explorer.CachedExplorerTransactionRepository;
import io.monke.app.internal.data.data.CacheManager;
import io.monke.app.internal.data.data.CachedRepository;
import io.monke.app.internal.di.annotations.Cached;
import io.monke.app.internal.storage.KVStorage;
import io.monke.app.storage.AccountStorage;
import io.monke.app.storage.AddressAccount;
import io.monke.app.storage.SecretStorage;
import network.minter.explorer.MinterExplorerApi;
import network.minter.explorer.models.DelegationInfo;
import network.minter.explorer.models.ExpResult;
import network.minter.explorer.models.HistoryTransaction;

@Module
public abstract class CacheModule {

    @Provides
    @WalletApp
    public static CacheManager provideCacheManager(@Cached Set<CachedRepository> cachedDependencies) {
        CacheManager cache = new CacheManager();
        cache.addAll(cachedDependencies);
        return cache;
    }

    // Just providing cached repositories
    @Provides
    @WalletApp
    public static CachedRepository<AddressAccount, AccountStorage> provideAccountStorage(AccountStorage accountStorage) {
        return new CachedRepository<>(accountStorage);
    }

    @Provides
    @WalletApp
    public static CachedRepository<List<HistoryTransaction>, CachedExplorerTransactionRepository> provideExplorerRepo(KVStorage storage, SecretStorage secretStorage, MinterExplorerApi api) {
        return new CachedRepository<>(new CachedExplorerTransactionRepository(storage, secretStorage, api.getApiService()));
    }

    @Provides
    @WalletApp
    public static CachedRepository<ExpResult<List<DelegationInfo>>, CachedExplorerAddressRepository> provideExplorerAddressRepo(KVStorage storage, SecretStorage secretStorage, MinterExplorerApi api) {
        return new CachedRepository<>(new CachedExplorerAddressRepository(storage, secretStorage, api.getApiService()));
    }

    // Bindings for CacheManager
    @Binds
    @IntoSet
    @Cached
    @WalletApp
    public abstract CachedRepository provideExplorerRepoForCache(CachedRepository<List<HistoryTransaction>, CachedExplorerTransactionRepository> cache);

    @Binds
    @IntoSet
    @Cached
    @WalletApp
    public abstract CachedRepository provideAccountStorageForCache(CachedRepository<AddressAccount, AccountStorage> cache);

    @Binds
    @IntoSet
    @Cached
    @WalletApp
    public abstract CachedRepository provideExplorerAddressrepoForCache(CachedRepository<ExpResult<List<DelegationInfo>>, CachedExplorerAddressRepository> cache);


}
