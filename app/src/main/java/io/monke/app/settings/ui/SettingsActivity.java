package io.monke.app.settings.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.inject.Provider;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.monke.app.R;
import io.monke.app.internal.BaseMvpInjectActivity;
import io.monke.app.settings.contract.SettingsView;
import io.monke.app.settings.views.SettingsPresenter;
import io.monke.app.setup.ui.DepositBottomDialog;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;
import network.minter.core.MinterSDK;

import static io.monke.app.internal.helpers.MathHelper.bdHuman;

public class SettingsActivity extends BaseMvpInjectActivity implements SettingsView {

    @Inject Provider<SettingsPresenter> presenterProvider;
    @InjectPresenter SettingsPresenter presenter;
    @BindView(R.id.toolbar) Toolbar toolbar;
    private Fragment mFragment;
    private DepositBottomDialog mDialog;

    @Override
    public void setBalance(BigDecimal totalBalance) {
        toolbar.setTitle(String.format("%s %s", bdHuman(totalBalance), MinterSDK.DEFAULT_COIN));
    }

    @Override
    public void setDelegatedBalance(BigDecimal delegatedAmount) {
        toolbar.setSubtitle(String.format("%s %s", bdHuman(delegatedAmount), MinterSDK.DEFAULT_COIN));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_deposit) {
            mDialog = DepositBottomDialog.newInstance(false);
            mDialog.show(getSupportFragmentManager(), null);
        }

        return false;
    }

    @ProvidePresenter
    SettingsPresenter providePresenter() {
        return presenterProvider.get();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        toolbar.inflateMenu(R.menu.settings_menu);

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_deposit) {
                mDialog = DepositBottomDialog.newInstance(false);
                mDialog.show(getSupportFragmentManager(), null);
            }

            return false;
        });

        mFragment = new SettingsFragment();

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .add(R.id.prefs_container, mFragment)
                .commit();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog != null) {
            try {
                mDialog.dismiss();
                mDialog = null;
            } catch (Throwable ignore) {
            }
        }
    }
}
