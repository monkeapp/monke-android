package io.monke.app.ime.screens.share;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.monke.app.R;
import timber.log.Timber;

public class ShareListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<ShareItem> mItems = new ArrayList<>();
    private LayoutInflater mInflater;
    private OnClickListener mListener;
    private View.OnClickListener mOnBackspaceClickListener;
    private View.OnLongClickListener mOnBackspaceLongClickListener;

    private enum ViewType {
        Share(R.layout.item_share),
        Backspace(R.layout.item_share_bsp);

        int v;

        ViewType(@LayoutRes int layout) {
            v = layout;
        }
    }

    public void setOnItemClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public void setItems(List<ShareItem> items) {
        mItems = items;
    }

    public List<ShareItem> getItems() {
        return mItems;
    }

    @Override
    public int getItemViewType(int position) {
        return position == mItems.size() ? ViewType.Backspace.ordinal() : ViewType.Share.ordinal();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }

        View view;
        if (viewType == ViewType.Share.ordinal()) {
            view = mInflater.inflate(ViewType.Share.v, parent, false);
            return new ShareViewHolder(view);
        } else {
            view = mInflater.inflate(ViewType.Backspace.v, parent, false);
            return new BackspaceViewHolder(view);
        }
    }

    public void enableBackspace(View.OnClickListener listener, View.OnLongClickListener longListener) {
        mOnBackspaceClickListener = listener;
        mOnBackspaceLongClickListener = longListener;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == mItems.size()) {
            BackspaceViewHolder vh = ((BackspaceViewHolder) holder);
            vh.itemView.setOnClickListener(v -> {
                if (mOnBackspaceClickListener != null) {
                    mOnBackspaceClickListener.onClick(v);
                }
            });
            vh.itemView.setOnLongClickListener(v -> {
                if (mOnBackspaceLongClickListener != null) {
                    return mOnBackspaceLongClickListener.onLongClick(v);
                }

                return false;
            });
        } else {
            final ShareItem item = mItems.get(holder.getAdapterPosition());
            ShareViewHolder vh = ((ShareViewHolder) holder);
            Timber.d("Share item handle %d", position);
            vh.title.setText(item.title);
            vh.itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onClick(v, mItems.get(holder.getAdapterPosition()));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mOnBackspaceClickListener != null ? mItems.size() + 1 : mItems.size();
    }

    public interface OnClickListener {
        void onClick(View view, ShareItem item);
    }

    public static class BackspaceViewHolder extends RecyclerView.ViewHolder {
        public BackspaceViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class ShareViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title) TextView title;

        public ShareViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
