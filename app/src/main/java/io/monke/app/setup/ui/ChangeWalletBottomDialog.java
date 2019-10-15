package io.monke.app.setup.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import io.monke.app.R;
import io.monke.app.apis.explorer.CachedExplorerAddressRepository;
import io.monke.app.internal.data.data.CachedRepository;
import io.monke.app.storage.AccountStorage;
import io.monke.app.storage.AddressAccount;
import io.monke.app.storage.SecretData;
import io.monke.app.storage.SecretStorage;
import network.minter.core.bip39.MnemonicResult;
import network.minter.core.bip39.NativeBip39;
import network.minter.core.crypto.MinterAddress;
import network.minter.explorer.models.DelegationInfo;
import network.minter.explorer.models.ExpResult;

/**
 * Monke. 2019
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class ChangeWalletBottomDialog extends BottomSheetDialogFragment {

    @BindView(R.id.title) TextView title;
    @BindView(R.id.mnemonic_input) EditText mnemonicInput;
    @BindView(R.id.address_text_container) ViewGroup addressTextRoot;
    @BindView(R.id.button_done) View actionDone;

    @Inject SecretStorage secretStorage;
    @Inject CachedRepository<AddressAccount, AccountStorage> accountStorage;
    @Inject
    CachedRepository<ExpResult<List<DelegationInfo>>, CachedExplorerAddressRepository> expAddressRepo;
    @Inject SharedPreferences prefs;
    private String mNewMnemonic;
    private OnWalletChangedListener mOnWalletChangedListener;

    public static ChangeWalletBottomDialog newInstance() {
        Bundle args = new Bundle();
        ChangeWalletBottomDialog fragment = new ChangeWalletBottomDialog();
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnWalletChangedListener(OnWalletChangedListener listener) {
        mOnWalletChangedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_change_wallet, container, false);
        ButterKnife.bind(this, view);

        mnemonicInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (NativeBip39.validateMnemonic(s.toString(), "en")) {
                    mNewMnemonic = s.toString();
                    actionDone.setEnabled(true);
                } else {
                    mNewMnemonic = null;
                    actionDone.setEnabled(false);
                }
            }
        });
        actionDone.setOnClickListener(this::onChangeWallet);

        return view;
    }

    private void onChangeWallet(View view) {
        if (mNewMnemonic == null || mNewMnemonic.isEmpty()) return;

        MinterAddress oldAddress = secretStorage.getAddresses().get(0);
        SecretData oldData = secretStorage.getSecret(oldAddress);
        secretStorage.remove(oldAddress);
        MinterAddress newAddress = secretStorage.add(new MnemonicResult(mNewMnemonic));

        oldData.cleanup();
        oldAddress.cleanup();

        accountStorage.clear();
        expAddressRepo.clear();

        if (mOnWalletChangedListener != null) {
            mOnWalletChangedListener.onChanged(newAddress);
        }

        dismiss();
    }

    public interface OnWalletChangedListener {
        void onChanged(MinterAddress address);
    }
}
