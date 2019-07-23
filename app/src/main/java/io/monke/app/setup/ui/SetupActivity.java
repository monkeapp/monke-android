package io.monke.app.setup.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.monke.app.R;
import io.monke.app.internal.BaseMvpInjectActivity;
import io.monke.app.internal.Monke;
import io.monke.app.settings.ui.SettingsActivity;
import io.monke.app.setup.adapters.SetupAdapter;
import io.monke.app.setup.contract.SetupView;
import io.monke.app.setup.views.SetupPresenter;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public class SetupActivity extends BaseMvpInjectActivity implements SetupView {

    @BindView(R.id.list) RecyclerView list;
    @BindView(R.id.testInput) EditText testInput;

    @Inject Provider<SetupPresenter> presenterProvider;
    @InjectPresenter SetupPresenter presenter;
    private DepositBottomDialog mDepositDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        if (Monke.ENABLE_CRASHLYTICS) {
            testInput.setVisibility(View.GONE);
        }
    }

    @Override
    public void setAdapter(SetupAdapter adapter) {
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
    }

    @Override
    public void startSystemKeyboardSettings(int requestCode) {
        startActivityForResult(
                new Intent(android.provider.Settings.ACTION_INPUT_METHOD_SETTINGS), requestCode);
    }

    @Override
    public void startSettings() {
        startActivityClearTop(this, SettingsActivity.class);
    }

    @Override
    public void showDepositDialog() {
        mDepositDialog = new DepositBottomDialog();
        mDepositDialog.show(getSupportFragmentManager(), null);
    }

    @ProvidePresenter
    SetupPresenter providePresenter() {
        return presenterProvider.get();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        presenter.onActivityResult(requestCode, resultCode, data);
    }

}
