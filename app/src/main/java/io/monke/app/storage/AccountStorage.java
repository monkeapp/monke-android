/*
 * Copyright (C) by MinterTeam. 2018
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

package io.monke.app.storage;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.monke.app.internal.data.data.CachedEntity;
import io.monke.app.internal.storage.KVStorage;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import network.minter.core.MinterSDK;
import network.minter.core.crypto.MinterAddress;
import network.minter.explorer.repo.ExplorerAddressRepository;

/**
 * minter-android-wallet. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class AccountStorage implements CachedEntity<AddressAccount> {
    private final static String KEY_BALANCE = "account_storage_balance";
    private final ExplorerAddressRepository mExpAddressRepo;
    private final SecretStorage mSecretStorage;
    private final KVStorage mStorage;


    public AccountStorage(KVStorage storage, SecretStorage secretStorage, ExplorerAddressRepository expAddressRepo) {
        mStorage = storage;
        mExpAddressRepo = expAddressRepo;
        mSecretStorage = secretStorage;
    }

    public static BigDecimal calcBalanceBase(List<AccountItem> items, final String coin, final MinterAddress address) {
        final Optional<BigDecimal> res = Stream.of(items)
                .filter(item -> item.getCoin().equals(coin))
                .filter(item -> item.getAddress().equals(address))
                .map(AccountItem::getBalanceBase)
                .reduce(BigDecimal::add);

        if (!res.isPresent()) {
            return new BigDecimal(0);
        }

        return res.get();
    }

    public static BigDecimal calcBalanceBase(List<AccountItem> items) {
        final Optional<BigDecimal> res = Stream.of(items)
                .map(AccountItem::getBalanceBase)
                .reduce(BigDecimal::add);

        if (!res.isPresent()) {
            return new BigDecimal(0);
        }

        return res.get();
    }

    /**
     * Group AccountItems's (inside AddressAccount) by coin, but reduces MinterAddress (it will be null after grouping)
     * Map does not changes original data
     * @return RxJava2 function
     */
    @SuppressWarnings("ConstantConditions")
    public static Function<AddressAccount, AddressAccount> groupAccountByCoin() {
        return items -> {
            List<AccountItem> in = new ArrayList<>(items.size());
            Stream.of(items.getAccountsItems()).forEach(item -> in.add(new AccountItem(item)));
            List<AccountItem> out = new ArrayList<>();
            final Map<String, AccountItem> tmp = new HashMap<>();
            for (AccountItem item : in) {
                if (!tmp.containsKey(item.coin)) {
//                    item.address = null;
                    tmp.put(item.coin, item);
                } else {
                    tmp.get(item.coin).balance = tmp.get(item.coin).balance.add(item.balance);
                }
            }

            Stream.of(tmp.values())
                    .forEach(out::add);

            return new AddressAccount(out);
        };
    }

    /**
     * Group AccountItems's by coin, but reduces MinterAddress (it will be null after grouping)
     * @return RxJava2 function
     */
    @SuppressWarnings("ConstantConditions")
    public static Function<List<AccountItem>, List<AccountItem>> groupAccountItemsByCoin() {
        return items -> {
            List<AccountItem> in = new ArrayList<>(items.size());
            Stream.of(items).forEach(item -> in.add(new AccountItem(item)));
            List<AccountItem> out = new ArrayList<>();
            final Map<String, AccountItem> tmp = new HashMap<>();
            for (AccountItem item : items) {
                if (!tmp.containsKey(item.coin)) {
//                    item.address = null;
                    tmp.put(item.coin, item);
                } else {
                    tmp.get(item.coin).balance = tmp.get(item.coin).balance.add(item.balance);
                }
            }

            Stream.of(tmp.values())
                    .forEach(out::add);

            return out;
        };
    }

    public AddressAccount getData() {
        return initialData();
    }

    @Override
    public AddressAccount initialData() {
        if (mStorage.contains(KEY_BALANCE)) {
            AddressAccount item = mStorage.get(KEY_BALANCE);
            if (item != null) {
                return item;
            }

            mStorage.delete(KEY_BALANCE);
        }

        AccountItem def = new AccountItem(MinterSDK.DEFAULT_COIN, mSecretStorage.getAddresses().get(0), BigDecimal.ZERO);
        return new AddressAccount(Collections.singletonList(def));
    }

    public List<AccountItem> getAccountItems() {
        if (mStorage.contains(KEY_BALANCE)) {
            return mStorage.<AddressAccount>get(KEY_BALANCE).getAccountsItems();
        }

        return Collections.emptyList();
    }

    public AddressAccount getAccount() {
        return initialData();
    }

    @Override
    public void onAfterUpdate(AddressAccount result) {
        mStorage.put(KEY_BALANCE, result);
    }

    @Override
    public void onClear() {
        mStorage.delete(KEY_BALANCE);
    }

    @Override
    public Observable<AddressAccount> getUpdatableData() {
        return ExplorerBalanceFetcher
                .create(mExpAddressRepo, mSecretStorage.getAddresses())
                .map(AddressAccount::new);
    }

}
