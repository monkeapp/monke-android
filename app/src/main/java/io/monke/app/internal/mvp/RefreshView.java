package io.monke.app.internal.mvp;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * Stars. 2017
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public interface RefreshView {
    void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener);
    void showRefreshProgress();
    void hideRefreshProgress();
}
