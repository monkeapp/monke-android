package io.monke.app.setup.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.monke.app.R;
import io.monke.app.internal.views.widgets.WalletButton;

public class SetupAdapter extends RecyclerView.Adapter<SetupAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private List<GuideItem> mItems;
    private int mExpandedPosition = 0;
    private OnActionClickListener mOnActionClickListener, mOnActionSecondClickListener;

    public SetupAdapter(List<GuideItem> items) {
        mItems = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }

        View v = mInflater.inflate(R.layout.row_guide_list_item, parent, false);

        return new ViewHolder(v);
    }

    public void expand(int position) {
        if (position > mItems.size() - 1) return;
        mExpandedPosition = position;
        notifyItemChanged(mExpandedPosition);
        notifyItemChanged(mExpandedPosition - 1);
    }

    public void setOnActionClickListener(OnActionClickListener listener) {
        mOnActionClickListener = listener;
    }

    public void setOnActionSecondClickListener(OnActionClickListener listener) {
        mOnActionSecondClickListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GuideItem item = mItems.get(holder.getAdapterPosition());
        holder.number.setText(String.valueOf(item.number));
        holder.title.setText(item.title);
        holder.action.setText(item.actionTitle);

        final boolean isExpanded = position == mExpandedPosition;
        holder.action.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.action.setActivated(isExpanded);

        holder.action.setOnClickListener(v -> {
            if (mOnActionClickListener != null) {
                mOnActionClickListener.onClick(v, mItems.get(holder.getAdapterPosition()));
            }
        });

        holder.actionSecond.setVisibility(isExpanded && item.actionSecondTitle != null ? View.VISIBLE : View.GONE);
        holder.actionSecond.setText(item.actionSecondTitle);
        holder.actionSecond.setOnClickListener(v -> {
            if (mOnActionSecondClickListener != null) {
                mOnActionSecondClickListener.onClick(v, mItems.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface OnActionClickListener {
        void onClick(View view, GuideItem item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.number) TextView number;
        @BindView(R.id.title) TextView title;
        @BindView(R.id.action) WalletButton action;
        @BindView(R.id.action_second) WalletButton actionSecond;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
