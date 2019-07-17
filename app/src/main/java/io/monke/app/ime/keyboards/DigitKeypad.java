package io.monke.app.ime.keyboards;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import io.monke.app.R;
import io.monke.app.ime.KeypadHandler;
import io.monke.app.internal.helpers.ViewHelper;

public class DigitKeypad extends KeypadHandler {

    @BindView(R.id.kp_row_0) ViewGroup row0;
    @BindView(R.id.kp_row_1) ViewGroup row1;
    @BindView(R.id.kp_row_2) ViewGroup row2;
    @BindView(R.id.kp_row_3) ViewGroup row3;

    private Map<String, KeyType> mSpecialKeys = new HashMap<String, KeyType>() {{
        put("backspace", KeyType.Backspace);
    }};

    private List<View> mKeys = new ArrayList<>(12);

    public DigitKeypad(ViewGroup rootView) {
        super(rootView);
        mKeys.addAll(ViewHelper.getChildrenList(row0));
        mKeys.addAll(ViewHelper.getChildrenList(row1));
        mKeys.addAll(ViewHelper.getChildrenList(row2));
        mKeys.addAll(ViewHelper.getChildrenList(row3));

        Stream.of(mKeys)
                .forEach(item -> {
                    item.setOnClickListener(v -> {
                        if (v.getTag() != null) {
                            String tagVal = (String) v.getTag();
                            if (mSpecialKeys.containsKey(tagVal)) {
                                KeyType kt = mSpecialKeys.get(tagVal);
                                if (kt == KeyType.Simple) {
                                    doOnKey(kt, tagVal);
                                } else {
                                    doOnKey(kt, null);
                                }
                            } else {
                                doOnKey(KeyType.Simple, tagVal);
                            }
                        } else if (v instanceof TextView) {
                            CharSequence val = ((TextView) v).getText();
                            if (val != null) {
                                doOnKey(KeyType.Simple, val.toString());
                            }
                        }
                    });
                });
    }
}
