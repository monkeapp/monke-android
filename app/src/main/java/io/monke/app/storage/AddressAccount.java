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

import org.parceler.Parcel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.MoreObjects.firstNonNull;


/**
 * minter-android-wallet. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
@Parcel
public class AddressAccount implements Serializable, Cloneable {
    List<AccountItem> mAccounts;
    BigDecimal mTotalBalance;
    BigDecimal mTotalBalanceUsd;
    BigDecimal mTotalBalanceBase;
    int mHashCode;

    public AddressAccount(List<AccountItem> accounts) {
        mAccounts = new ArrayList<>(accounts);
        mTotalBalance = new BigDecimal(0);
        mTotalBalanceUsd = new BigDecimal(0);
        mTotalBalanceBase = new BigDecimal(0);
        for (AccountItem item : mAccounts) {
            mTotalBalance = mTotalBalance.add(item.getBalance());
            mTotalBalanceUsd = mTotalBalanceUsd.add(item.getBalanceUsd());
            mTotalBalanceBase = mTotalBalanceBase.add(item.getBalanceBase());
        }
        mHashCode = Objects.hash(mAccounts, mTotalBalance, mTotalBalanceUsd, mTotalBalanceBase);
    }

    public AddressAccount(List<AccountItem> accounts, BigDecimal totalBalance, BigDecimal totalBalanceUsd, BigDecimal totalBalanceBase) {
        mAccounts = new ArrayList<>(accounts);
        mTotalBalance = firstNonNull(totalBalance, new BigDecimal(0));
        mTotalBalanceUsd = firstNonNull(totalBalanceUsd, new BigDecimal(0));
        mTotalBalanceBase = firstNonNull(totalBalanceBase, new BigDecimal(0));
        mHashCode = Objects.hash(mAccounts, mTotalBalance, mTotalBalanceUsd, mTotalBalanceBase);
    }

    AddressAccount() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressAccount that = (AddressAccount) o;
        return Objects.equals(mAccounts, that.mAccounts) &&
                Objects.equals(mTotalBalance, that.mTotalBalance) &&
                Objects.equals(mTotalBalanceUsd, that.mTotalBalanceUsd) &&
                Objects.equals(mTotalBalanceBase, that.mTotalBalanceBase);
    }

    @Override
    public int hashCode() {
        return mHashCode;
    }

    public int size() {
        return mAccounts != null ? mAccounts.size() : 0;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public BigDecimal getTotalBalance() {
        if (mTotalBalance == null) {
            mTotalBalance = new BigDecimal(0);
        }
        return mTotalBalance;
    }

    public BigDecimal getTotalBalanceBase() {
        if (mTotalBalanceBase == null) {
            mTotalBalanceBase = new BigDecimal(0);
        }
        return mTotalBalanceBase;
    }

    public BigDecimal getTotalBalanceUsd() {
        if (mTotalBalanceUsd == null) {
            mTotalBalanceUsd = new BigDecimal(0);
        }
        return mTotalBalanceUsd;
    }

    public Optional<AccountItem> findAccountItemByCoin(String coin) {
        return Stream.of(getAccountsItems())
                .filter(item -> item.getCoin().equals(coin))
                .findFirst();
    }

    public Optional<AccountItem> findByCoin(String coin) {
        return Stream.of(getAccountsItems()).filter(item -> item.coin.toLowerCase().equals(coin.toLowerCase())).findFirst();
    }

    public AccountItem getFirstAccountItem() {
        if (getAccountsItems().isEmpty()) {
            return null;
        }

        return getAccountsItems().get(0);
    }

    public List<AccountItem> getAccountsItems() {
        if (mAccounts == null) {
            mAccounts = Collections.emptyList();
        }

        return mAccounts;
    }

}
