package io.monke.app.splash.views;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.monke.app.internal.Monke;
import io.monke.app.internal.PrefKeys;
import io.monke.app.internal.helpers.QRAddressGenerator;
import io.monke.app.internal.mvp.MvpBasePresenter;
import io.monke.app.splash.contract.SplashView;
import io.monke.app.storage.SecretStorage;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import moxy.InjectViewState;
import network.minter.core.crypto.MinterAddress;
import timber.log.Timber;

@InjectViewState
public class SplashPresenter extends MvpBasePresenter<SplashView> {

    @Inject SecretStorage secretStorage;
    @Inject SharedPreferences prefs;

    @Inject
    public SplashPresenter() {

    }

    @Override
    public void attachView(SplashView view) {
        super.attachView(view);
        if (secretStorage.getSecretsStream().count() == 0) {
            createAccount();
            return;
        }

        Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    if (prefs.getInt(PrefKeys.SETUP_LAST_STEP, 1) >= 3) {
                        getViewState().startSettings();
                    } else {
                        getViewState().startSetup();
                    }

                });


    }

    @SuppressLint("ApplySharedPref")
    private void createAccount() {
        MinterAddress address = secretStorage.add(SecretStorage.generateAddress());
        Timber.d("Generated address: %s", address.toString());

        // 38% of screen is qr code image
        float qrWidth = ((float) Monke.app().display().getWidth()) * 0.388f;
        QRAddressGenerator.create((int) qrWidth, address.toString())
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    if (res.bitmap == null) {
                        Timber.e("Unable to create QR: Unknown reason.");
                        return;
                    }
                    Timber.d("QR created: %s", res.file.toString());
                    prefs.edit().putString(PrefKeys.QR_BITMAP_PATH, res.file.toString()).commit();

                    getViewState().startSetup();
                }, t -> {
                    Timber.e(t, "Unable to generate QR image");
                });
    }
}
