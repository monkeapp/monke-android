package io.monke.app.settings.contract;

import java.math.BigDecimal;

import androidx.recyclerview.widget.RecyclerView;
import moxy.MvpView;

public interface SettingsView extends MvpView {
    void setBalance(BigDecimal totalBalance);
    void setDelegatedBalance(BigDecimal delegatedAmount);
    void setAdapter(RecyclerView.Adapter<?> adapter);
    void startRateApp();
    void startBackup();
    void startAbout();
    void startTelegram();
    void restartApplication();
}
