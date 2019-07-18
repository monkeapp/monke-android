package io.monke.app.setup.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.monke.app.R;
import io.monke.app.internal.Monke;
import io.monke.app.internal.PrefKeys;
import io.monke.app.internal.mvp.MvpBasePresenter;
import io.monke.app.setup.adapters.GuideItem;
import io.monke.app.setup.adapters.SetupAdapter;
import io.monke.app.setup.contract.SetupView;
import moxy.InjectViewState;

@InjectViewState
public class SetupPresenter extends MvpBasePresenter<SetupView> {

    public final static int STEP_1 = 1;
    public final static int STEP_2 = 2;
    public final static int STEP_3 = 3;

    public static final int REQUEST_ENABLE_MONKE_KEYBOARD = 1000;

    @Inject Resources res;
    @Inject SharedPreferences prefs;
    private SetupAdapter mAdapter;

    @Inject
    public SetupPresenter() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_MONKE_KEYBOARD) {
            if (resultCode == Activity.RESULT_OK) {
                mAdapter.expand(STEP_2);
            }
        }
    }

    @Override
    public void attachView(SetupView view) {
        super.attachView(view);
    }


    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();

        List<GuideItem> items = new ArrayList<GuideItem>() {{
            add(new GuideItem(STEP_1, res.getString(R.string.guide_step_1), res.getString(R.string.guide_step_1_button)));
            add(new GuideItem(STEP_2, res.getString(R.string.guide_step_2), res.getString(R.string.guide_step_2_button)));
            add(new GuideItem(STEP_3, res.getString(R.string.guide_step_3), res.getString(R.string.guide_step_3_button), res.getString(R.string.guide_step_3_skip)));
        }};

        int lastStep = 0;
        if (prefs.contains(PrefKeys.SETUP_LAST_STEP)) {
            lastStep = prefs.getInt(PrefKeys.SETUP_LAST_STEP, STEP_1);
        }

        mAdapter = new SetupAdapter(items);
        mAdapter.setOnActionClickListener((view, item) -> {
            /*

             */

            prefs.edit().putInt(PrefKeys.SETUP_LAST_STEP, item.number).apply();

            switch (item.number) {
                case STEP_1:
                    getViewState().startSystemKeyboardSettings(REQUEST_ENABLE_MONKE_KEYBOARD);
                    break;

                case STEP_2:
                    InputMethodManager mgr = (InputMethodManager) Monke.app().context().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (mgr != null) {
                        mgr.showInputMethodPicker();
                    }
                    mAdapter.expand(STEP_3);
                    break;

                case STEP_3:
                    getViewState().showDepositDialog();
                    break;
            }


            mAdapter.expand(item.number);
        });

        mAdapter.setOnActionSecondClickListener((view, item) -> {
            if (item.number == STEP_3) {
                getViewState().startSettings();
            }
        });
        getViewState().setAdapter(mAdapter);

        if (lastStep > 0) {
            mAdapter.expand(lastStep);
        }

    }
}
