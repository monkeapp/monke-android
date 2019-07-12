package io.monke.app.splash.ui;

import android.os.Bundle;

import javax.inject.Inject;
import javax.inject.Provider;

import io.monke.app.R;
import io.monke.app.internal.BaseMvpInjectActivity;
import io.monke.app.setup.ui.SetupActivity;
import io.monke.app.splash.contract.SplashView;
import io.monke.app.splash.views.SplashPresenter;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public class SplashActivity extends BaseMvpInjectActivity implements SplashView {

    @Inject Provider<SplashPresenter> presenterProvider;
    @InjectPresenter SplashPresenter presenter;

    @Override
    public void startSetup() {
        startActivityClearTop(this, SetupActivity.class);
    }

    @ProvidePresenter
    SplashPresenter providePresenter() {
        return presenterProvider.get();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }
}
