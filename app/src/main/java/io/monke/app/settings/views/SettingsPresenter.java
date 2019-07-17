package io.monke.app.settings.views;

import javax.inject.Inject;

import io.monke.app.internal.mvp.MvpBasePresenter;
import io.monke.app.settings.contract.SettingsView;
import moxy.InjectViewState;

@InjectViewState
public class SettingsPresenter extends MvpBasePresenter<SettingsView> {

    @Inject
    public SettingsPresenter() {

    }
}
