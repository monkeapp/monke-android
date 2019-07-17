package io.monke.app.ime.screens;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Stream;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import io.monke.app.R;
import io.monke.app.account.AccountSelectedAdapter;
import io.monke.app.ime.MonkeKeyboard;
import io.monke.app.ime.keyboards.DigitKeypad;
import io.monke.app.ime.keyboards.HexKeypad;
import io.monke.app.ime.screens.share.ShareItem;
import io.monke.app.ime.screens.share.ShareListAdapter;
import io.monke.app.internal.forms.DecimalInputFilter;
import io.monke.app.internal.forms.InputGroup;
import io.monke.app.internal.forms.validators.RegexValidator;
import io.monke.app.internal.helpers.MathHelper;
import io.monke.app.internal.views.widgets.BipCircleImageView;
import io.monke.app.storage.AccountItem;
import io.monke.app.storage.AddressAccount;
import io.monke.app.tx.TxSendHandler;
import network.minter.blockchain.models.TransactionSendResult;
import network.minter.core.MinterSDK;
import network.minter.core.crypto.MinterAddress;
import network.minter.explorer.MinterExplorerApi;
import network.minter.explorer.models.GateResult;
import network.minter.profile.MinterProfileApi;
import timber.log.Timber;

import static android.content.Context.CLIPBOARD_SERVICE;
import static io.monke.app.internal.helpers.MathHelper.bdGT;
import static io.monke.app.internal.helpers.MathHelper.bdGTE;
import static io.monke.app.internal.helpers.MathHelper.bdHuman;
import static io.monke.app.internal.helpers.MathHelper.bdNull;

public class SendScreen extends BaseScreen {
    @BindView(R.id.balance_available) TextView balanceAvailable;
    @BindView(R.id.fragment_container) FrameLayout fragmentContainer;
    @BindView(R.id.keypad_hex) ViewGroup keyboardHex;
    @BindView(R.id.keypad_digits) ViewGroup keyboardNumpad;
    @BindView(R.id.share_list) RecyclerView shareList;
    @BindView(R.id.share_container) ViewGroup shareContainer;
    @BindView(R.id.amount_input_container) ViewGroup inputAmountContainer;
    @BindView(R.id.coin_icon) BipCircleImageView coinIcon;
    @BindView(R.id.coin_selector) RecyclerView coinList;
    @BindView(R.id.submit) Button submit;
    @BindView(R.id.action_copy) View buttonCopy;
    @BindView(R.id.action_use_max) View buttonUseMax;
    @BindView(R.id.input_amount) EditText inputAmount;
    @BindView(R.id.input_address) EditText inputAddress;
    @BindView(R.id.input_coin) EditText inputCoin;
    @BindView(R.id.dummy_input) EditText dummyInput;
    @BindView(R.id.address_icon) BipCircleImageView addressIcon;

    @Inject TxSendHandler tx;

    private DigitKeypad mDigitKeypad;
    private HexKeypad mHexKeypad;
    private InputGroup mInputGroup = new InputGroup();
    private String mLastCoin = MinterSDK.DEFAULT_COIN;
    private AccountSelectedAdapter mCoinListAdapter = new AccountSelectedAdapter();
    private ShareListAdapter mShareListAdapter = new ShareListAdapter();
    private boolean mFocusedInteraction = false;
    private boolean mFormValid = false;


    public enum FocusWidget {
        Address,
        Coin,
        Amount,
        Share,
    }

    @Inject
    public SendScreen() {

    }

    @Override
    protected void onInit(MonkeKeyboard keyboard, View rootView) {
        super.onInit(keyboard, rootView);
        keyboard.addOnUpdateAccountListener(this::onUpdateAccount);
        tx.init(keyboard.getAccount());

        setAvailable(BigDecimal.ZERO);

        clearWidgetFocus();

        mDigitKeypad = new DigitKeypad(keyboardNumpad);
        mDigitKeypad.attachInput(inputAmount);

        {
            mHexKeypad = new HexKeypad(keyboardHex);
            mHexKeypad.attachInput(inputAddress);
        }

        {
            coinList.setLayoutManager(new LinearLayoutManager(getContext()));
            coinList.setAdapter(mCoinListAdapter);
            mCoinListAdapter.notifyDataSetChanged();
            mCoinListAdapter.setOnClickListener(item -> {
                coinIcon.setImageUrl(item.getAvatar());
                inputCoin.setText(item.getCoin());
                tx.setAccount(item);
                setAvailable(item.getBalance());
                clearWidgetFocus();
            });
        }

        {
            FlexboxLayoutManager shareLayoutManager = new FlexboxLayoutManager(getContext());
            shareLayoutManager.setFlexDirection(FlexDirection.ROW);
            shareLayoutManager.setJustifyContent(JustifyContent.CENTER);
            shareList.setLayoutManager(shareLayoutManager);
            mShareListAdapter.setOnItemClickListener((view, item) -> {
                InputConnection inputConnection = getKeyboard().getCurrentInputConnection();
                inputConnection.commitText(item.meta != null ? item.meta : item.title, 0);
            });
            shareList.setAdapter(mShareListAdapter);
        }


        buttonUseMax.setOnClickListener(this::onClickUseMax);

        inputCoin.setText(mLastCoin);


        inputAmount.setOnFocusChangeListener((view, hasFocus) -> focusOn(FocusWidget.Amount));
        inputAmount.setOnClickListener(this::onClickAmount);

        inputAddress.setOnFocusChangeListener((v, hasFocus) -> focusOn(FocusWidget.Address));
        inputAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable != null && editable.length() > 0 && inputCoin.getText() != null && inputCoin.getText().length() >= 3) {
                    if (MinterAddress.testString(editable.toString())) {
                        coinIcon.setImageUrl(MinterProfileApi.getCoinAvatarUrl(inputCoin.getText().toString()));
                    }
                }
            }
        });

        inputCoin.setOnClickListener(v -> {
            if (getKeyboard().getAccount() == null) {
                Toast.makeText(getContext(), "Unable to select coin: internet connection problem", Toast.LENGTH_LONG).show();
                clearWidgetFocus();
                return;
            }
            focusOn(FocusWidget.Coin);
        });

        mInputGroup.addInput(inputAmount);
        mInputGroup.addInput(inputAddress);
        mInputGroup.addValidator(inputAmount, new RegexValidator("^(\\d*)(\\.)?(\\d{1,18})$", "Invalid number", false));
        mInputGroup.addValidator(inputAddress, new RegexValidator(MinterAddress.ADDRESS_PATTERN, false));
        mInputGroup.addFilter(inputAmount, new DecimalInputFilter(() -> inputAmount));
        mInputGroup.addFormValidateListener(valid -> {
            mFormValid = valid;
            if (!mFocusedInteraction) {
                if (bdGTE(getKeyboard().getAccount().get().getBalance(), tx.getAmount())) {
                    submit.setEnabled(valid);
                    if (valid) {
                        submit.setOnClickListener(this::startExecuteTransaction);
                    } else {
                        submit.setOnClickListener(null);
                    }
                }
            }
        });
        mInputGroup.addTextChangedListener((editText, valid) -> {
            View parent = (View) editText.getParent();
            if (!valid) {
                parent.setBackgroundColor(getResources().getColor(R.color.errorField));
            } else {
                parent.setBackgroundColor(getResources().getColor(R.color.white));
            }

            if (editText.getId() == R.id.input_address && !valid) {
                addressIcon.setVisibility(View.GONE);
            }

            if (!valid || editText.getText().length() == 0) return;

            switch (editText.getId()) {
                case R.id.input_amount:
                    BigDecimal amount = MathHelper.bigDecimalFromString(editText.getText().toString());
                    if (bdNull(amount)) {
                        mInputGroup.setError("amount", "Can't be 0");
                        parent.setBackgroundColor(getResources().getColor(R.color.errorField));
                        break;
                    } else if (bdGT(amount, tx.getAccount().get().getBalance())) {
                        mInputGroup.setError("amount", "Not enough balance");
                        parent.setBackgroundColor(getResources().getColor(R.color.errorField));
                        break;
                    } else {
                        parent.setBackgroundColor(getResources().getColor(R.color.white));
                        mInputGroup.setError("amount", null);
                    }

                    tx.setAmount(amount);
                    break;

                case R.id.input_address:
                    tx.setRecipient(new MinterAddress(editText.getText().toString()));
                    addressIcon.setImageUrl(MinterProfileApi.getUserAvatarUrlByAddress(tx.getRecipient()));
                    addressIcon.setVisibility(View.VISIBLE);
                    break;
            }
        });
        buttonCopy.setOnClickListener(this::tryToPasteMinterAddressFromCB);
        submit.setOnClickListener(this::startExecuteTransaction);
    }

    private void startExecuteTransaction(View view) {
        getKeyboard().showProgress(true);
        submit.setEnabled(false);
        unsubscribeOnDestroy(tx.send().subscribe(this::onExecuteSuccess, this::onExecuteFailed));
    }


    private void onExecuteFailed(final Throwable t) {
        getKeyboard().setError(t.getMessage());
        submit.setEnabled(true);
    }

    private void onExecuteError(GateResult<?> errorResult) {
        getKeyboard().setError(errorResult.getMessage());
        submit.setEnabled(true);
    }

    private void onExecuteSuccess(final GateResult<TransactionSendResult> result) {
        Timber.d("Execute Success: %b", result.isOk());
        if (!result.isOk()) {
            onExecuteError(result);
            return;
        }

//        tx.buyBanana(tx.getAmount(), getKeyboard().getBananaAccount().get().getBalance());

        getKeyboard().showProgress(false);
        mInputGroup.clearFields();
        mInputGroup.clearErrors();


        final String[] shareTitles = getResources().getStringArray(R.array.share_titles);
        final List<ShareItem> shareItems = new ArrayList<>(shareTitles.length + 1);
        Stream.of(shareTitles).forEach(item -> shareItems.add(new ShareItem(item)));
        shareItems.add(new ShareItem(
                getResources().getString(R.string.share_title_tx, result.result.txHash.toShortString()),
                MinterExplorerApi.newFrontUrl().addPathSegment("transaction").addPathSegment(result.result.txHash.toString()).toString()
        ));

        mShareListAdapter.setItems(shareItems);
        mShareListAdapter.notifyDataSetChanged();

        getKeyboard().updateBalance(true);

        focusOn(FocusWidget.Share);

        getKeyboard().showCloseButton(true, v -> {
            clearWidgetFocus();
            getKeyboard().showCloseButton(false);
            submit.setVisibility(View.VISIBLE);
        });
    }

    private void updateCoins(List<AccountItem> accounts) {
        mCoinListAdapter.setItems(accounts);
        mCoinListAdapter.notifyDataSetChanged();
    }

    private void onUpdateAccount(AddressAccount account) {
        tx.setAccount(account.getFirstAccountItem());
        updateCoins(account.getAccountsItems());
        setAvailable(account.getTotalBalance());
    }

    private void clearWidgetFocus() {
        Timber.d("Clear widget focus");
        dummyInput.requestFocus();

        inputAmount.clearFocus();
        inputCoin.clearFocus();
        inputAddress.clearFocus();
        keyboardNumpad.setVisibility(View.GONE);
        keyboardHex.setVisibility(View.GONE);
        coinList.setVisibility(View.GONE);
        shareContainer.setVisibility(View.GONE);
        mFocusedInteraction = false;
        submit.setText(R.string.btn_send);
        submit.setEnabled(mFormValid);


        if (mFormValid) {
            submit.setOnClickListener(this::startExecuteTransaction);
        } else {
            submit.setOnClickListener(null);
        }
    }

    private void focusOn(FocusWidget widget) {
        getKeyboard().showCloseButton(false);
        Timber.d("Focus on: %s", widget.name());
        mFocusedInteraction = true;
        submit.setVisibility(View.VISIBLE);
        submit.setEnabled(true);
        submit.setText(R.string.btn_ok);
        submit.setOnClickListener(v -> {
            clearWidgetFocus();
        });

        switch (widget) {
            case Coin:
                keyboardNumpad.setVisibility(View.GONE);
                keyboardHex.setVisibility(View.GONE);
                shareContainer.setVisibility(View.GONE);
                coinList.setVisibility(View.VISIBLE);
                break;
            case Amount:
                keyboardHex.setVisibility(View.GONE);
                coinList.setVisibility(View.GONE);
                shareContainer.setVisibility(View.GONE);
                keyboardNumpad.setVisibility(View.VISIBLE);
                break;
            case Address:
                keyboardNumpad.setVisibility(View.GONE);
                coinList.setVisibility(View.GONE);
                shareContainer.setVisibility(View.GONE);
                keyboardHex.setVisibility(View.VISIBLE);
                break;
            case Share:
                keyboardNumpad.setVisibility(View.GONE);
                coinList.setVisibility(View.GONE);
                keyboardHex.setVisibility(View.GONE);
                shareContainer.setVisibility(View.VISIBLE);
                submit.setVisibility(View.GONE);
                shareList.requestLayout();
                break;
        }

    }

    private void onClickUseMax(View view) {
        if (getKeyboard().getAccount() == null || bdNull(tx.getAccount().get().getBalance())) {
            inputAmount.setText("0");
            return;
        }
        inputAmount.setText(tx.getAccount().get().getBalance().toPlainString());
    }


    private void setAvailable(BigDecimal value) {
        balanceAvailable.setText(getResources().getString(R.string.balance_available, bdHuman(value), MinterSDK.DEFAULT_COIN));
    }

    private void onClickAmount(View view) {
        Timber.d("OnClickSomething");
        focusOn(FocusWidget.Amount);
    }

    private void tryToPasteMinterAddressFromCB(View view) {
        Pattern pattern = Pattern.compile("(Mx[a-fA-F0-9]{40})", Pattern.CASE_INSENSITIVE);

        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        if (cm == null) {
            return;
        }

        ClipData cd = cm.getPrimaryClip();
        if (cd == null) return;

        for (int i = 0; i < cd.getItemCount(); i++) {
            ClipData.Item item = cd.getItemAt(i);
            CharSequence raw = item.getText();
            Matcher matcher = pattern.matcher(raw.toString());
            if (matcher.find()) {
                String val = matcher.group();
                inputAddress.setText(val);
                inputAddress.setSelection(val.length());
                break;
            }

        }
    }
}