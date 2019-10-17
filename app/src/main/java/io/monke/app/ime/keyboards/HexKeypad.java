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

public class HexKeypad extends KeypadHandler {

    @BindView(R.id.digits_container) ViewGroup row0;
    @BindView(R.id.letter_container) ViewGroup row1;
    @BindView(R.id.b7) View backspace;

    private Map<String, KeyType> mSpecialKeys = new HashMap<String, KeyType>() {{
        put("backspace", KeyType.Backspace);
    }};

    public HexKeypad(ViewGroup rootView) {
        super(rootView);
        final List<View> keys = new ArrayList<>(18);
        keys.addAll(ViewHelper.getChildrenList(row0));
        keys.addAll(ViewHelper.getChildrenList(row1));
        keys.add(backspace);

        Stream.of(keys)
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

                    item.setOnLongClickListener(v -> {
                        if (v.getTag() != null) {
                            String tagVal = (String) v.getTag();
                            if (mSpecialKeys.containsKey(tagVal)) {
                                KeyType kt = mSpecialKeys.get(tagVal);
                                if (kt == KeyType.Simple) {
                                    return doOnLongKey(kt, tagVal);
                                } else {
                                    return doOnLongKey(kt, null);
                                }
                            } else {
                                return doOnLongKey(KeyType.Simple, tagVal);
                            }
                        } else if (v instanceof TextView) {
                            CharSequence val = ((TextView) v).getText();
                            if (val != null) {
                                return doOnLongKey(KeyType.Simple, val.toString());
                            }
                        }

                        return false;
                    });
                });
    }
}
