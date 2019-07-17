package io.monke.app.ime;

import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.ButterKnife;

import static com.google.common.base.MoreObjects.firstNonNull;
import static io.monke.app.internal.common.Preconditions.checkArgument;

public abstract class KeypadHandler {

    private OnKeyListener mOnKeyListener;

    public enum KeyType {
        Simple(-1),
        Backspace(KeyEvent.KEYCODE_DEL),
        Enter(KeyEvent.KEYCODE_ENTER),

        ;

        int mKeyCode;

        KeyType(int code) {
            mKeyCode = code;
        }

        public int getKeyCode() {
            return mKeyCode;
        }
    }

    public KeypadHandler(ViewGroup rootView) {
        ButterKnife.bind(this, rootView);
    }

    public void setOnKeyListener(OnKeyListener listener) {
        mOnKeyListener = listener;
    }

    public void attachInput(@NonNull final EditText editText) {
        checkArgument(editText != null, "EditText must be set");

        setOnKeyListener((type, value) -> {
            CharSequence src = firstNonNull(editText.getText(), "");
            String txt = src.toString();
            int inputPos = txt.length();

            if (type == KeyType.Simple) {
                txt += value;
                inputPos = txt.length();
            } else {
                switch (type) {
                    case Backspace:
                        if (!txt.isEmpty()) {
                            int pos = editText.getSelectionStart();
                            if (pos <= 1) {
                                txt = "";
                                inputPos = pos;
                                break;
                            } else if (pos == txt.length()) {
                                txt = txt.substring(0, txt.length() - 1);
                                inputPos = txt.length();
                            } else {
                                String newValue = txt.substring(0, pos - 1);
                                newValue += txt.substring(pos);
                                txt = newValue;
                                inputPos = pos - 1;
                            }
                        }
                        break;
                    case Enter:
                        txt += "\n";
                        break;
                }
            }

            editText.setText(txt);
            if (!txt.isEmpty()) {
                editText.setSelection(inputPos);
            }

        });
    }

    protected void doOnKey(KeyType type, String value) {
        if (mOnKeyListener != null) {
            mOnKeyListener.onKey(type, value);
        }
    }

    public interface OnKeyListener {
        void onKey(KeyType type, @Nullable String value);
    }

}
