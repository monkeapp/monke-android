package io.monke.app.setup.contract;

import io.monke.app.setup.adapters.SetupAdapter;
import moxy.MvpView;

public interface SetupView extends MvpView {

    void setAdapter(SetupAdapter adapter);
    void startSystemKeyboardSettings(int requestCode);
    void finish();
    void startSettings();
    void showDepositDialog();
}
