package io.monke.app.settings.views;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.monke.app.R;
import io.monke.app.internal.common.Lazy;
import io.monke.app.internal.views.list.MultiRowAdapter;
import io.monke.app.internal.views.list.MultiRowContract;

import static io.monke.app.internal.helpers.ViewHelper.visible;

/**
 * Monke. 2019
 * @author Eduard Maximovich [edward.vstock@gmail.com]
 */
public class SettingsItemRow implements MultiRowContract.Row<SettingsItemRow.ViewHolder> {
    private final ItemData mData;

    SettingsItemRow(ItemData data) {
        mData = data;
    }

    @Override
    public int getItemView() {
        return R.layout.row_settings_item;
    }

    @Override
    public int getRowPosition() {
        return 0;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder) {
        visible(viewHolder.icon, mData.mIcon != 0);
        if (mData.mIcon != 0) {
            viewHolder.icon.setImageResource(mData.mIcon);
        }

        visible(viewHolder.subtitle, mData.mSubtitle != null);
        viewHolder.subtitle.setText(mData.mSubtitle);

        viewHolder.title.setText(mData.mTitle);

        viewHolder.itemView.setOnClickListener(v -> {
            if (mData.mOnItemClickListener != null) {
                mData.mOnItemClickListener.onClick(mData);
            }
        });

        visible(viewHolder.flag, mData.mSwitchListener != null);
        if (mData.mCheckedFlag != null) {
            viewHolder.flag.setChecked(mData.mCheckedFlag.get());
        }
        viewHolder.flag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mData.mSwitchListener != null) {
                    mData.mSwitchListener.onCheckedChanged(buttonView, isChecked);
                }
            }
        });
    }

    @Override
    public void onUnbindViewHolder(@NonNull ViewHolder viewHolder) {

    }

    @NonNull
    @Override
    public Class<ViewHolder> getViewHolderClass() {
        return ViewHolder.class;
    }

    public interface OnItemClickListener {
        void onClick(ItemData item);
    }

    public static final class ViewHolder extends MultiRowAdapter.RowViewHolder {
        @BindView(R.id.item_icon) ImageView icon;
        @BindView(R.id.item_title) TextView title;
        @BindView(R.id.item_subtitle) TextView subtitle;
        @BindView(R.id.item_switch) Switch flag;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public final static class ItemData {
        private int mIcon;
        private CharSequence mTitle;
        private CharSequence mSubtitle;
        private OnItemClickListener mOnItemClickListener;
        private Switch.OnCheckedChangeListener mSwitchListener;
        private Lazy<Boolean> mCheckedFlag;

        public ItemData(int icon, CharSequence title, CharSequence subtitle, OnItemClickListener listener) {
            mIcon = icon;
            mTitle = title;
            mSubtitle = subtitle;
            mOnItemClickListener = listener;
        }

        public ItemData setSwitchListener(Switch.OnCheckedChangeListener listener) {
            mSwitchListener = listener;
            return this;
        }

        public ItemData setChecked(Lazy<Boolean> checkedValue) {
            mCheckedFlag = checkedValue;
            return this;
        }
    }

    public static class Builder {
        private final Context mContext;
        private List<ItemData> mData = new LinkedList<>();

        public Builder(Context context) {
            mContext = context;
        }

        public Builder addItem(ItemData itemData) {
            mData.add(itemData);
            return this;
        }

        public Builder addItem(CharSequence title, OnItemClickListener clickListener) {
            return addItem(title, null, clickListener);
        }

        public Builder addItem(@StringRes int titleRes, OnItemClickListener clickListener) {
            return addItem(mContext.getString(titleRes), null, clickListener);
        }

        public Builder addItem(@DrawableRes int icon, CharSequence title, CharSequence subtitle, OnItemClickListener listener) {
            mData.add(new ItemData(icon, title, subtitle, listener));
            return this;
        }

        public Builder addItem(CharSequence title, CharSequence subtitle, OnItemClickListener listener) {
            return addItem(0, title, subtitle, listener);
        }

        public Builder addItem(@DrawableRes int icon, @StringRes int title, @StringRes int subtitle, OnItemClickListener listener) {
            return addItem(icon, mContext.getString(title), mContext.getString(subtitle), listener);
        }

        public Builder addItem(@StringRes int title, @StringRes int subtitle, OnItemClickListener listener) {
            return addItem(0, title, subtitle, listener);
        }

        public List<SettingsItemRow> build() {
            List<SettingsItemRow> rows = new LinkedList<>();
            for (ItemData item : mData) {
                rows.add(new SettingsItemRow(item));
            }

            return rows;
        }
    }
}
