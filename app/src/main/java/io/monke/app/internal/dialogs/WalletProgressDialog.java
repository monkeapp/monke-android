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
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.monke.app.R;
import io.monke.app.internal.common.DeferredCall;

/**
 * minter-android-wallet. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class WalletProgressDialog extends WalletDialog {
    @BindView(R.id.dialog_text) TextView text;
    private DeferredCall<WalletProgressDialog> mDefer = DeferredCall.createWithSize(1);
    private Builder mBuilder;

    protected WalletProgressDialog(@NonNull Context context, Builder builder) {
        super(context);
        mBuilder = builder;
    }

    public void setText(@StringRes int resId) {
        mDefer.call(ctx -> {
            runOnUiThread(() -> ctx.text.setText(resId));
        });
    }

    public void setText(CharSequence text, Object... args) {
        setText(String.format(text.toString(), args));
    }

    public void setText(CharSequence text) {
        mDefer.call(ctx -> {
            runOnUiThread(() -> ctx.text.setText(text));
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_progress_dialog);
        ButterKnife.bind(this);
        mDefer.attach(this);

        title.setText(mBuilder.mTitle);
        if (mBuilder.mText == null) {
            text.setText(R.string.attention_please_wait);
        } else {
            text.setText(mBuilder.mText);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDefer.detach();
    }

    public static final class Builder extends WalletDialogBuilder<WalletProgressDialog, Builder> {
        private CharSequence mText;

        public Builder(Context context, @StringRes int title) {
            super(context, title);
        }

        public Builder(Context context, CharSequence title) {
            super(context, title);
        }

        public Builder setText(CharSequence text) {
            mText = text;
            return this;
        }

        public Builder setText(@StringRes int textRes) {
            return setText(getContext().getResources().getString(textRes));
        }

        @Override
        public WalletProgressDialog create() {
            return new WalletProgressDialog(getContext(), this);
        }
    }
}
