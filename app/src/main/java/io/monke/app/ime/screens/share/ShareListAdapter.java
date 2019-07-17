package io.monke.app.ime.screens.share;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.monke.app.R;
import timber.log.Timber;

public class ShareListAdapter extends RecyclerView.Adapter<ShareListAdapter.ViewHolder> {

    private List<ShareItem> mItems = new ArrayList<>();
    private LayoutInflater mInflater;
    private OnClickListener mListener;

    public void setOnItemClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public void setItems(List<ShareItem> items) {
        mItems = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mInflater == null) {
            mInflater = LayoutInflater.from(parent.getContext());
        }

        View view = mInflater.inflate(R.layout.item_share, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final ShareItem item = mItems.get(holder.getAdapterPosition());

        Timber.d("Share item handle %d", position);
        holder.title.setText(item.title);
        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick(v, mItems.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public interface OnClickListener {
        void onClick(View view, ShareItem item);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.title) TextView title;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
