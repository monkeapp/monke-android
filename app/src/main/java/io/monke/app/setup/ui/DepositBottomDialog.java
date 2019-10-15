package io.monke.app.setup.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import io.monke.app.R;
import io.monke.app.internal.PrefKeys;
import io.monke.app.internal.helpers.ContextHelper;
import io.monke.app.internal.helpers.HtmlCompat;
import io.monke.app.internal.helpers.ViewHelper;
import io.monke.app.settings.ui.SettingsActivity;
import io.monke.app.storage.SecretStorage;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import network.minter.core.crypto.MinterAddress;
import timber.log.Timber;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * Monke. 2019
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class DepositBottomDialog extends BottomSheetDialogFragment {

    private final static String ARG_DISMISS_OR_START_SETTINGS = "ARG_DIMSISS_OR_START_SETTINGS";
    private final static String ARG_TITLE = "ARG_TITLE";
    private final static String ARG_QR = "ARG_QR";
    private final static String ARG_ADDRESS = "ARG_ADDRESS";
    private final static String ARG_SHOW_ADDRESS_ICON = "ARG_SHOW_ADDRESS_ICON";

    @BindView(R.id.title) TextView title;
    @BindView(R.id.qr) ImageView qrImage;
    @BindView(R.id.address_text) TextView addressText;
    @BindView(R.id.address_text_container) ViewGroup addressTextRoot;
    @BindView(R.id.address_icon) ImageView addressIcon;
    @BindView(R.id.button_copy) View actionCopy;
    @BindView(R.id.button_done) View actionDone;

    @Inject SecretStorage secretStorage;
    @Inject SharedPreferences prefs;
    private boolean mStartSettings = true;
    private boolean mShowAddressIcon = false;
    private String mAddress;

    public static DepositBottomDialog newInstance(boolean startSettings) {
        Bundle args = new Bundle();
        args.putBoolean(ARG_DISMISS_OR_START_SETTINGS, startSettings);

        DepositBottomDialog fragment = new DepositBottomDialog();
        fragment.setArguments(args);
        return fragment;
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
        View view = inflater.inflate(R.layout.dialog_deposit, container, false);
        ButterKnife.bind(this, view);

        CharSequence tTitle;
        String tAddress;
        String tQRPath;

        Bundle args = firstNonNull(getArguments(), new Bundle());
        mStartSettings = args.getBoolean(ARG_DISMISS_OR_START_SETTINGS, true);
        mShowAddressIcon = args.getBoolean(ARG_SHOW_ADDRESS_ICON, false);
        tTitle = args.getCharSequence(ARG_TITLE, HtmlCompat.fromHtml(getString(R.string.deposit_title)));
        mAddress = args.getString(ARG_ADDRESS, secretStorage.getAddresses().get(0).toString());
        tQRPath = args.getString(ARG_QR, prefs.getString(PrefKeys.QR_BITMAP_PATH, null));


        ViewHelper.visible(addressIcon, mShowAddressIcon);
        if (mShowAddressIcon) {
            addressText.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        } else {
            addressText.setGravity(Gravity.CENTER);
        }
        title.setText(tTitle);
        addressText.setText(mAddress);
        addressTextRoot.setOnClickListener(this::onCopyAddress);
        actionCopy.setOnClickListener(this::onCopyAddress);
        actionDone.setOnClickListener(this::onDone);

        Observable
                .create((ObservableOnSubscribe<Bitmap>) emitter -> {
                    Bitmap bmp;
                    try {
                        bmp = BitmapFactory.decodeFile(tQRPath);
                    } catch (Throwable t) {
                        emitter.onError(t);
                        return;
                    }

                    emitter.onNext(bmp);
                    emitter.onComplete();
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(res -> qrImage.setImageBitmap(res), t -> Timber.e(t, "Unable to load QR image"));

        return view;
    }

    private void onDone(View view) {
        if (mStartSettings) {
            ((SetupActivity) getActivity()).startActivityClearTop(getActivity(), SettingsActivity.class);
        } else {
            dismiss();
        }
    }

    private void onCopyAddress(View view) {
        ContextHelper.copyToClipboard(getActivity(), mAddress);

    }

    public static final class Builder {
        private boolean mStartSettings;
        private CharSequence mTitle;
        private String mQrPath;
        private String mAddress;
        private boolean mShowAddressIcon = false;

        public Builder(boolean startSettings) {
            mStartSettings = startSettings;
        }

        public Builder setTitle(CharSequence title) {
            mTitle = title;
            return this;
        }

        public Builder setShowAddressIcon(boolean show) {
            mShowAddressIcon = show;
            return this;
        }

        public Builder setQRPath(String qrPath) {
            mQrPath = qrPath;
            return this;
        }

        public Builder setAddress(MinterAddress address) {
            mAddress = address.toString();
            return this;
        }

        public Builder setAddress(String address) {
            mAddress = address;
            return this;
        }

        public DepositBottomDialog build() {
            final DepositBottomDialog dialog = new DepositBottomDialog();
            Bundle args = new Bundle();

            args.putBoolean(ARG_DISMISS_OR_START_SETTINGS, mStartSettings);

            if (mTitle != null) {
                args.putCharSequence(ARG_TITLE, mTitle);
            }

            if (mQrPath != null) {
                args.putString(ARG_QR, mQrPath);
            }

            if (mAddress != null) {
                args.putString(ARG_ADDRESS, mAddress);
            }

            args.putBoolean(ARG_SHOW_ADDRESS_ICON, mShowAddressIcon);

            dialog.setArguments(args);
            return dialog;
        }
    }
}
