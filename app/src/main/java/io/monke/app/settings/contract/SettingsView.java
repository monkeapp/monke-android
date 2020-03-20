package io.monke.app.settings.contract;

import android.content.Intent;
import android.text.Spanned;

import java.math.BigDecimal;

import androidx.recyclerview.widget.RecyclerView;
import io.monke.app.setup.ui.ChangeWalletBottomDialog;
import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface SettingsView extends MvpView {
    void setBalance(BigDecimal totalBalance);
    void setDelegatedBalance(BigDecimal delegatedAmount);
    void setAdapter(RecyclerView.Adapter<?> adapter);
    void startRateApp();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void startBackup(int requestCode);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void startBackupView();
    void startAbout();
    void startTelegram();
    void restartApplication();

    @StateStrategyType(OneExecutionStateStrategy.class)
    void startDonationDialog(Spanned title, String address, String qrPath);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void startTransactionsList(String address);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void startChangeWalletDialog(ChangeWalletBottomDialog.OnWalletChangedListener listener);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void startIntent(Intent intent);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void startBuyBanana();

}
