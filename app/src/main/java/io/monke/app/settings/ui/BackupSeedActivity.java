package io.monke.app.settings.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.monke.app.R;
import io.monke.app.internal.BaseMvpInjectActivity;
import io.monke.app.internal.helpers.ContextHelper;
import io.monke.app.internal.helpers.HtmlCompat;
import io.monke.app.storage.SecretStorage;

public class BackupSeedActivity extends BaseMvpInjectActivity {

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.backup_desc) TextView desc;
    @BindView(R.id.seed) TextView seed;
    @BindView(R.id.action_copy) View actionCopy;

    @Inject SecretStorage secretStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_seed);
        ButterKnife.bind(this);

        setupToolbar(toolbar);

        desc.setText(HtmlCompat.fromHtml(getString(R.string.backup_description)));
        seed.setText(secretStorage.getSecretsStream().findFirst().get().getValue().getSeedPhrase());
        actionCopy.setOnClickListener(v -> {
            ContextHelper.copyToClipboard(this, secretStorage.getSecretsStream().findFirst().get().getValue().getSeedPhrase());
        });
    }
}
