package io.monke.app.setup.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.monke.app.R;
import io.monke.app.internal.BaseMvpInjectActivity;
import io.monke.app.internal.dialogs.WalletDialog;
import io.monke.app.internal.forms.DecimalInputFilter;
import io.monke.app.internal.forms.InputGroup;
import io.monke.app.internal.forms.validators.EmptyValidator;
import io.monke.app.internal.forms.validators.RegexValidator;
import io.monke.app.internal.views.widgets.BipCircleImageView;
import io.monke.app.setup.contract.BuyBananaView;
import io.monke.app.setup.views.BuyBananaPresenter;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static io.monke.app.internal.helpers.MathHelper.bdHuman;

public class BuyBananaActivity extends BaseMvpInjectActivity implements BuyBananaView {

    @Inject Provider<BuyBananaPresenter> presenterProvider;
    @InjectPresenter BuyBananaPresenter presenter;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.submit) Button submit;
    @BindView(R.id.input_spend_amount) TextInputEditText inputSpend;
    @BindView(R.id.input_get_amount) TextInputEditText inputGet;
    @BindView(R.id.coin_spend) TextView coinSpend;
    @BindView(R.id.coin_spend_icon) BipCircleImageView coinSpendIcon;
    @BindView(R.id.error_text) TextView errorText;
    @BindView(R.id.bip_progress) ImageView progress;
    @BindView(R.id.fee_value) TextView feeValue;
    private InputGroup mInputGroup;
    private WalletDialog mCurrentDialog;
    private CharSequence mSubmitText = null;

    @ProvidePresenter
    public BuyBananaPresenter presenterProvider() {
        return presenterProvider.get();
    }

    @Override
    public void setBalance(BigDecimal totalBalance, String coin) {
        toolbar.setTitle(String.format("Balance: %s %s", bdHuman(totalBalance), coin));
    }

    @Override
    public void setFormValidationListener(InputGroup.OnFormValidateListener listener) {
        mInputGroup.addFormValidateListener(listener);
    }

    @Override
    public void showProgress(boolean show) {
        submit.setClickable(!show);
        if (show) {
            mSubmitText = submit.getText();
            submit.setText(null);
        } else {
            submit.setText(mSubmitText);
        }

        if (show) {
            RotateAnimation rotate = new RotateAnimation(
                    0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
            );

            rotate.setDuration(600);
            rotate.setRepeatCount(Animation.INFINITE);

            progress.startAnimation(rotate);
        } else {
            progress.clearAnimation();
            progress.setAnimation(null);
        }

        progress.setVisibility(show ? View.VISIBLE : View.INVISIBLE);

    }

    @Override
    public void startIntent(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void finishSuccess() {
        finish();
    }

    @Override
    public void setFee(String feeText) {
        feeValue.setText(feeText);
    }

    @Override
    public void setSubmitEnabled(boolean enabled) {
        submit.setEnabled(enabled);
    }

    @Override
    public void setError(CharSequence message) {
        if (message == null) {
            errorText.setVisibility(View.GONE);
            errorText.setText(null);
        } else {
            errorText.setText(message);
            errorText.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setTextChangedListener(InputGroup.OnTextChangedListener listener) {
        mInputGroup.addTextChangedListener(listener);
    }

    @Override
    public void setGetAmount(BigDecimal amount) {
        inputGet.setText(bdHuman(amount));
    }

    @Override
    public void setCoin(String coin) {
        coinSpend.setText(coin);
    }

    @Override
    public void setCoinIcon(String iconUrl) {
        coinSpendIcon.setImageUrl(iconUrl);
    }

    @Override
    public void setOnCoinClickListener(View.OnClickListener listener) {
        coinSpend.setOnClickListener(listener);
        coinSpendIcon.setOnClickListener(listener);
    }

    @Override
    public void startDialog(WalletDialog.DialogExecutor executor) {
        mCurrentDialog = WalletDialog.switchDialogWithExecutor(this, mCurrentDialog, executor);
    }

    @Override
    public void setOnSubmit(View.OnClickListener listener) {
        submit.setOnClickListener(listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WalletDialog.releaseDialog(mCurrentDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_banana);
        ButterKnife.bind(this);
        setupToolbar(toolbar);
        mInputGroup = new InputGroup();
        mInputGroup.setErrorView(this::setError);
        mInputGroup.addInput(inputGet, inputSpend);
        RegexValidator decimalValidator = new RegexValidator("^(\\d*)(\\.)?(\\d{1,18})?$", "Invalid number", true);
        mInputGroup.addValidator(inputGet, decimalValidator);
        mInputGroup.addValidator(inputSpend, decimalValidator);
        mInputGroup.addFilter(inputGet, new DecimalInputFilter(() -> inputGet));
        mInputGroup.addFilter(inputSpend, new DecimalInputFilter(() -> inputSpend));
        mInputGroup.addValidator(inputGet, new EmptyValidator("Value can't be empty"));
        mInputGroup.addValidator(inputSpend, new EmptyValidator("Value can't be empty"));
    }
}
