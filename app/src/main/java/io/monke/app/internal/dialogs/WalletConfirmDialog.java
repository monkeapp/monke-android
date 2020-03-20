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

package io.monke.app.internal.dialogs;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.monke.app.R;
import io.monke.app.apis.reactive.ReactiveMyMinter;
import io.monke.app.internal.helpers.ExceptionHelper;
import io.monke.app.internal.helpers.ViewHelper;
import network.minter.core.internal.exceptions.NetworkException;
import network.minter.profile.models.ProfileResult;
import retrofit2.HttpException;

import static io.monke.app.internal.common.Preconditions.checkNotNull;
import static io.monke.app.internal.helpers.ViewHelper.visible;


/**
 * minter-android-wallet. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public final class WalletConfirmDialog extends WalletDialog {

    private final Builder mBuilder;
    @BindView(R.id.dialog_text) TextView text;
    @BindView(R.id.dialog_description) TextView description;
    @BindView(R.id.action_confirm) Button actionConfirm;
    @BindView(R.id.action_decline) Button actionDecline;

    public WalletConfirmDialog(@NonNull Context context, Builder builder) {
        super(context);
        mBuilder = builder;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_confirm_dialog);
        ButterKnife.bind(this);
        title.setText(mBuilder.mTitle);

        visible(description, mBuilder.mDescription);
        if (mBuilder.mDescription != null) {
            description.setText(mBuilder.mDescription);
        }

        text.setText(mBuilder.mText);
        text.setTextAlignment(mBuilder.mTextAlignment);
        text.setTextIsSelectable(mBuilder.mTextIsSelectable);
        if (mBuilder.mOnTextClickListener != null) {
            text.setClickable(true);
            text.setFocusable(true);
            ViewHelper.setSelectableItemBackground(text);
            text.setOnClickListener(mBuilder.mOnTextClickListener);
        }

        if (mBuilder.mTypeface != null) {
            text.setTypeface(mBuilder.mTypeface);
        }
        if (mBuilder.mDescriptionTypeface != null) {
            description.setTypeface(mBuilder.mDescriptionTypeface);
        }

        actionConfirm.setText(mBuilder.getPositiveTitle());
        actionConfirm.setOnClickListener(v -> {
            if (mBuilder.hasPositiveListener()) {
                mBuilder.getPositiveListener().onClick(WalletConfirmDialog.this, BUTTON_POSITIVE);
            } else {
                dismiss();
            }
        });

        if (mBuilder.mNegativeTitle != null) {
            actionDecline.setText(mBuilder.getNegativeTitle());
            actionDecline.setOnClickListener(v -> {
                if (mBuilder.hasNegativeListener()) {
                    mBuilder.getNegativeListener().onClick(WalletConfirmDialog.this, BUTTON_NEGATIVE);
                } else {
                    dismiss();
                }
            });
        } else {
            actionDecline.setVisibility(View.GONE);
        }
    }

    public static final class Builder extends WalletDialogBuilder<WalletConfirmDialog, WalletConfirmDialog.Builder> {
        private CharSequence mText;
        private CharSequence mDescription;
        private boolean mTextIsSelectable = false;
        private Typeface mTypeface;
        private Typeface mDescriptionTypeface;
        private View.OnClickListener mOnTextClickListener;
        private int mTextAlignment = View.TEXT_ALIGNMENT_INHERIT;

        public Builder(Context context, @StringRes int title) {
            super(context, title);
        }

        public Builder(Context context, CharSequence title) {
            super(context, title);
        }

        @Override
        public WalletConfirmDialog create() {
            checkNotNull(mPositiveTitle, "At least, positive action title should be set");
            return new WalletConfirmDialog(getContext(), this);
        }

        public Builder setText(CharSequence text) {
            mText = text;
            return this;
        }

        public Builder setText(@StringRes int resId) {
            return setText(getContext().getString(resId));
        }

        public Builder setText(String text, Object... args) {
            mText = String.format(text, args);
            return this;
        }

        public Builder setDescription(CharSequence description) {
            mDescription = description;
            return this;
        }

        public Builder setDescription(@StringRes int resId) {
            return setDescription(getContext().getString(resId));
        }

        public Builder setOnTextClickListener(View.OnClickListener listener) {
            mOnTextClickListener = listener;
            return this;
        }

        public Builder setTextIsSelectable(boolean isSelectable) {
            mTextIsSelectable = isSelectable;
            return this;
        }

        public Builder setText(Throwable t) {
            if (t instanceof HttpException) {
                if (((HttpException) t).code() >= 500 && ((HttpException) t).code() < 1000) {
                    setTitle(mTitle + " (server error " + ((HttpException) t).code() + ")");
                } else if (((HttpException) t).code() < 500 && ((HttpException) t).code() > 0) {
                    setTitle(mTitle + " (client error " + ((HttpException) t).code() + ")");
                } else {
                    setTitle(mTitle + " (network error " + ((HttpException) t).code() + ")");
                }
                try {
                    String out = ((HttpException) t).response().errorBody().string() + "\n";
                    ProfileResult errorResult = ReactiveMyMinter.createProfileError(((HttpException) t));
                    out += errorResult.getError().message + "\n" + errorResult.getError().message + "\n" + ExceptionHelper.getStackTrace(t);
                    mText = out;
                } catch (IOException e) {
                    e.printStackTrace();
                    mText = ((HttpException) t).message() + "\n" + ExceptionHelper.getStackTrace(t);
                }

            } else if (t instanceof NetworkException) {
                final int statusCode = ((NetworkException) t).getStatusCode();
                if (statusCode >= 500 && statusCode < 1000) {
                    setTitle(mTitle + " (server error " + statusCode + ")");
                } else if (statusCode < 500 && statusCode > 0) {
                    setTitle(mTitle + " (client error " + statusCode + ")");
                } else {
                    setTitle(mTitle + " (network error " + statusCode + ")");
                }
                mText = t.getMessage();

            } else {
                mText = t.getMessage() + "\n" + ExceptionHelper.getStackTrace(t);
            }

            return this;
        }

        public Builder setPositiveAction(@StringRes int titleRes) {
            return setPositiveAction(titleRes, null);
        }

        public Builder setPositiveAction(CharSequence title) {
            return setPositiveAction(title, null);
        }

        public Builder setNegativeAction(@StringRes int titleRes) {
            return setNegativeAction(titleRes, null);
        }

        public Builder setNegativeAction(CharSequence title) {
            return setNegativeAction(title, null);
        }

        public Builder setNegativeAction(@StringRes int titleRes, OnClickListener listener) {
            return super.setAction(BUTTON_NEGATIVE, mContext.get().getResources().getString(titleRes), listener);
        }

        public Builder setNegativeAction(CharSequence title, OnClickListener listener) {
            return super.setAction(BUTTON_NEGATIVE, title, listener);
        }

        public Builder setPositiveAction(@StringRes int titleRes, OnClickListener listener) {
            return setPositiveAction(mContext.get().getResources().getString(titleRes), listener);
        }

        public Builder setPositiveAction(CharSequence title, OnClickListener listener) {
            return super.setAction(BUTTON_POSITIVE, title, listener);
        }

        public Builder setDescriptionTypeface(Typeface typeface) {
            mDescriptionTypeface = typeface;
            return this;
        }

        public Builder setTextTypeface(Typeface typeface) {
            mTypeface = typeface;
            return this;
        }

        public Builder setTextAlignment(int textAlignment) {
            mTextAlignment = textAlignment;
            return this;
        }
    }


}
