package io.monke.app.ime.screens;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import io.monke.app.ime.MonkeKeyboard;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BaseScreen {

    private CompositeDisposable mSubscriptions = new CompositeDisposable();
    private WeakReference<MonkeKeyboard> mKeyboard;

    public final void init(MonkeKeyboard keyboard, View rootView) {
        mKeyboard = new WeakReference<>(keyboard);
        ButterKnife.bind(this, rootView);
        onInit(getKeyboard(), rootView);
    }

    public final void destroy() {
        if (mKeyboard != null && mKeyboard.get() != null) {
            mKeyboard.clear();
        }

        if (!mSubscriptions.isDisposed()) {
            mSubscriptions.dispose();
        }
        mKeyboard.clear();
        onDestroy();
    }

    public MonkeKeyboard getKeyboard() {
        return mKeyboard != null && mKeyboard.get() != null ? mKeyboard.get() : null;
    }

    protected void unsubscribeOnDestroy(Disposable subscription) {
        mSubscriptions.add(subscription);
    }

    protected void onDestroy() {
    }

    protected void onInit(MonkeKeyboard keyboard, View rootView) {
    }

    protected Resources getResources() {
        return getContext().getResources();
    }

    protected Context getContext() {
        return getKeyboard().getApplicationContext();
    }
}
