package io.monke.app.settings.contract;

import java.math.BigDecimal;

import moxy.MvpView;

public interface SettingsView extends MvpView {
    void setBalance(BigDecimal totalBalance);
    void setDelegatedBalance(BigDecimal delegatedAmount);
}
