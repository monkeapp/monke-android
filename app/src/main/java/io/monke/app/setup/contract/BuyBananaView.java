package io.monke.app.setup.contract;

import android.content.Intent;
import android.view.View;

import java.math.BigDecimal;

import io.monke.app.internal.dialogs.WalletDialog;
import io.monke.app.internal.forms.InputGroup;
import moxy.MvpView;
import moxy.viewstate.strategy.OneExecutionStateStrategy;
import moxy.viewstate.strategy.StateStrategyType;

public interface BuyBananaView extends MvpView {

    void setBalance(BigDecimal totalBalance, String coin);
    void setFormValidationListener(InputGroup.OnFormValidateListener listener);
    void setSubmitEnabled(boolean enabled);
    void setError(CharSequence message);
    void setTextChangedListener(InputGroup.OnTextChangedListener listener);
    void setGetAmount(BigDecimal amount);
    void setCoin(String coin);
    void setCoinIcon(String iconUrl);
    void setOnCoinClickListener(View.OnClickListener listener);
    @StateStrategyType(OneExecutionStateStrategy.class)
    void startDialog(WalletDialog.DialogExecutor executor);
    void setOnSubmit(View.OnClickListener listener);
    void showProgress(boolean show);
    void startIntent(Intent intent);
    void finishSuccess();
    void setFee(String feeText);
}
