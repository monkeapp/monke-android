package io.monke.app.internal.views.widgets;

import android.content.Context;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.StringRes;

/**
 * Monke. 2019
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class ProgressButtonWrapper {

    private final Button mButton;
    private final ProgressBar mProgress;
    private final Context mContext;
    private CharSequence mPrevText = null;

    public ProgressButtonWrapper(Button button, ProgressBar progress) {
        mButton = button;
        mProgress = progress;
        mContext = button.getContext();
        mPrevText = mButton.getText();
    }

    public void setText(CharSequence text) {
        mButton.setText(text);
    }

    public void setText(@StringRes int resId) {
        mButton.setText(resId);
    }

    public void setProgress(boolean progress) {
        if (progress) {
            mPrevText = mButton.getText();
            mButton.setText(null);
        }

    }
}
