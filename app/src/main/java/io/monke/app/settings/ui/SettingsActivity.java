package io.monke.app.settings.ui;

import android.os.Bundle;

import javax.inject.Inject;
import javax.inject.Provider;

import io.monke.app.internal.BaseMvpInjectActivity;
import io.monke.app.settings.contract.SettingsView;
import io.monke.app.settings.views.SettingsPresenter;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public class SettingsActivity extends BaseMvpInjectActivity implements SettingsView {

    @Inject Provider<SettingsPresenter> presenterProvider;
    @InjectPresenter SettingsPresenter presenter;

    @ProvidePresenter
    SettingsPresenter providePresenter() {
        return presenterProvider.get();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
}
