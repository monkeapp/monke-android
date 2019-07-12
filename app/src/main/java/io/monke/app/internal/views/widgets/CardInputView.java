package io.monke.app.internal.views.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.monke.app.R;
import io.sweers.barber.Barber;
import io.sweers.barber.Kind;
import io.sweers.barber.StyledAttr;

/**
 * Monke. 2019
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class CardInputView extends FrameLayout {

    @StyledAttr(value = R.styleable.CardInputView_civ_hint)
    CharSequence mHint;
    @StyledAttr(R.styleable.CardInputView_civ_text)
    CharSequence mText;
    @StyledAttr(value = R.styleable.CardInputView_civ_prefix_icon, kind = Kind.RES_ID)
    int mPrefixIcon;
    @StyledAttr(value = R.styleable.CardInputView_civ_suffix_icon, kind = Kind.RES_ID)
    int mSuffixIcon;
    @StyledAttr(value = R.styleable.CardInputView_civ_suffix_use_button)
    boolean mUseSuffixButton = false;
    @StyledAttr(value = R.styleable.CardInputView_civ_suffix_text)
    CharSequence mSuffixButtonText;
    @StyledAttr(value = R.styleable.CardInputView_civ_background, kind = Kind.COLOR)
    int backgroundColor = -1;

    @BindView(R.id.civ_action_button) Button suffixButton;
    @BindView(R.id.civ_action) ImageView action;
    @BindView(R.id.civ_input) AppCompatEditText input;
    @BindView(R.id.civ_left_icon) ImageView prefix;

    public CardInputView(Context context) {
        super(context);
    }

    public CardInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0, 0);
    }

    public CardInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr, 0);
    }

    public CardInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, defStyleAttr, defStyleRes);
    }

    private void init(AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        inflate(getContext(), R.layout.card_input_view, this);

        Barber.style(this, attrs, R.styleable.CardInputView, defStyleAttr, defStyleRes);
        ButterKnife.bind(this);

        if (mPrefixIcon > 0) {
            prefix.setImageResource(mPrefixIcon);
        }
    }

    public Button getSuffixButton() {
        return suffixButton;
    }

    public ImageView getAction() {
        return action;
    }

    public AppCompatEditText getInput() {
        return input;
    }

    public void setUseSuffixButton(boolean use) {
        mUseSuffixButton = use;
        requestLayout();
    }

    public ImageView getPrefix() {
        return prefix;
    }
}
