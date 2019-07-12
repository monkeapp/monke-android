/*
 * Copyright (C) by MinterTeam. 2019
 * @link <a href="https://github.com/MinterTeam">Org Github</a>
 * @link <a href="https://github.com/edwardstock">Maintainer Github</a>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.monke.app.internal.di;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.google.gson.GsonBuilder;

import java.util.List;

import javax.inject.Named;

import dagger.Component;
import io.monke.app.apis.explorer.CachedExplorerAddressRepository;
import io.monke.app.apis.explorer.CachedExplorerTransactionRepository;
import io.monke.app.internal.Monke;
import io.monke.app.internal.data.data.CachedRepository;
import io.monke.app.internal.helpers.DisplayHelper;
import io.monke.app.internal.helpers.FingerprintHelper;
import io.monke.app.internal.helpers.ImageHelper;
import io.monke.app.internal.helpers.NetworkHelper;
import io.monke.app.internal.storage.KVStorage;
import io.monke.app.storage.AccountStorage;
import io.monke.app.storage.SecretStorage;
import io.monke.app.storage.UserAccount;
import network.minter.blockchain.repo.BlockChainAccountRepository;
import network.minter.blockchain.repo.BlockChainBlockRepository;
import network.minter.blockchain.repo.BlockChainCoinRepository;
import network.minter.blockchain.repo.BlockChainStatusRepository;
import network.minter.blockchain.repo.BlockChainTransactionRepository;
import network.minter.core.internal.api.ApiService;
import network.minter.explorer.models.DelegationInfo;
import network.minter.explorer.models.ExpResult;
import network.minter.explorer.models.HistoryTransaction;
import network.minter.explorer.repo.ExplorerAddressRepository;
import network.minter.explorer.repo.ExplorerCoinsRepository;
import network.minter.explorer.repo.ExplorerTransactionRepository;
import network.minter.explorer.repo.GateEstimateRepository;
import network.minter.explorer.repo.GateGasRepository;
import network.minter.explorer.repo.GateTransactionRepository;

/**
 * minter-android-wallet. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
@Component(modules = {
        MonkeModule.class,
        HelpersModule.class,
        RepoModule.class,
        InjectorsModule.class,
        CacheModule.class,
})
@WalletApp
public interface MonkeComponent {

    void inject(Monke app);

    // app
    Context context();
    Resources res();

    ApiService.Builder apiBuilder();

    KVStorage storage();

    @Named("uuid")
    String uuid();

    // helpers
    DisplayHelper display();
    NetworkHelper network();
    ImageHelper image();
    SharedPreferences prefs();
    GsonBuilder gsonBuilder();

    FingerprintHelper fingerprint();

    // notification

    // repositories
    // local
    SecretStorage secretStorage();
    AccountStorage accountStorage();
    CachedRepository<UserAccount, AccountStorage> accountStorageCache();
    CachedRepository<List<HistoryTransaction>, CachedExplorerTransactionRepository> explorerTransactionsRepoCache();
    CachedRepository<ExpResult<List<DelegationInfo>>, CachedExplorerAddressRepository> explorerAddressRepoCache();


    // explorer
    ExplorerTransactionRepository explorerTransactionsRepo();
    ExplorerAddressRepository addressExplorerRepo();
    ExplorerCoinsRepository explorerCoinsRepo();
    GateGasRepository gasRepo();
    GateTransactionRepository txGateRepo();
    GateEstimateRepository estimateRepo();
    // blockchain
    BlockChainAccountRepository accountRepoBlockChain();
    BlockChainCoinRepository coinRepoBlockChain();
    BlockChainTransactionRepository txRepoBlockChain();
    BlockChainStatusRepository statusRepoBlockChain();
    BlockChainBlockRepository bcBlockRepo();

    // test
//    IdlingManager idlingManager();
}
