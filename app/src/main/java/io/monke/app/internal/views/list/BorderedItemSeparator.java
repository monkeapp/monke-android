/*
 * Copyright (C) by MinterTeam. 2018
 * @link <a href="https://github.com/MinterTeam">Org Github</a>
 * @link <a href="https://github.com/edwardstock">Maintainer Github</a>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.monke.app.internal.views.list;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * minter-android-wallet. 2018
 * @author Eduard Maximovich <edward.vstock@gmail.com>
 */
public class BorderedItemSeparator extends RecyclerView.ItemDecoration {

    int mOrientation = -1;
    private Drawable mDivider;
    private boolean mShowFirstDivider = false;
    private boolean mShowLastDivider = false;

    public BorderedItemSeparator(Context context, AttributeSet attrs) {
        final TypedArray a = context
                .obtainStyledAttributes(attrs, new int[]{android.R.attr.listDivider});
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    public BorderedItemSeparator(Context context, AttributeSet attrs, boolean showFirstDivider,
                                 boolean showLastDivider) {
        this(context, attrs);
        mShowFirstDivider = showFirstDivider;
        mShowLastDivider = showLastDivider;
    }

    public BorderedItemSeparator(Context context, int resId) {
        mDivider = ContextCompat.getDrawable(context, resId);
    }

    public BorderedItemSeparator(Context context, int resId, boolean showFirstDivider,
                                 boolean showLastDivider) {
        this(context, resId);
        mShowFirstDivider = showFirstDivider;
        mShowLastDivider = showLastDivider;
    }

    public BorderedItemSeparator(Drawable divider) {
        mDivider = divider;
    }

    public BorderedItemSeparator(Drawable divider, boolean showFirstDivider,
                                 boolean showLastDivider) {
        this(divider);
        mShowFirstDivider = showFirstDivider;
        mShowLastDivider = showLastDivider;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mDivider == null) {
            return;
        }

        int position = parent.getChildAdapterPosition(view);
        if (position == RecyclerView.NO_POSITION || (position == 0 && !mShowFirstDivider)) {
            return;
        }

        if (mOrientation == -1)
            getOrientation(parent);

        if (mOrientation == LinearLayoutManager.VERTICAL) {
            outRect.top = 1;
            if (mShowLastDivider && position == (state.getItemCount() - 1)) {
                outRect.bottom = outRect.top;
            }
        } else {
            outRect.left = mDivider.getIntrinsicWidth();
            if (mShowLastDivider && position == (state.getItemCount() - 1)) {
                outRect.right = outRect.left;
            }
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mDivider == null) {
            super.onDrawOver(c, parent, state);
            return;
        }

        // Initialization needed to avoid compiler warning
        int left = 0, right = 0, top = 0, bottom = 0, size;
        int orientation = mOrientation != -1 ? mOrientation : getOrientation(parent);
        int childCount = parent.getChildCount();

        if (orientation == LinearLayoutManager.VERTICAL) {
            size = mDivider.getIntrinsicHeight();
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
        } else { //horizontal
            size = mDivider.getIntrinsicWidth();
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
        }

        for (int i = mShowFirstDivider ? 0 : 1; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            if (orientation == LinearLayoutManager.VERTICAL) {
                top = child.getTop() - params.topMargin - size;
                bottom = top + size;
            } else { //horizontal
                left = child.getLeft() - params.leftMargin;
                right = left + size;
            }
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }

        // show last divider
        if (mShowLastDivider && childCount > 0) {
            View child = parent.getChildAt(childCount - 1);
            if (parent.getChildAdapterPosition(child) == (state.getItemCount() - 1)) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
                if (orientation == LinearLayoutManager.VERTICAL) {
                    top = child.getBottom() + params.bottomMargin;
                    bottom = top + size;
                } else { // horizontal
                    left = child.getRight() + params.rightMargin;
                    right = left + size;
                }
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

    private int getOrientation(RecyclerView parent) {
        if (mOrientation == -1) {
            if (parent.getLayoutManager() instanceof LinearLayoutManager) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
                mOrientation = layoutManager.getOrientation();
            } else {
                throw new IllegalStateException(
                        "DividerItemDecoration can only be used with a LinearLayoutManager.");
            }
        }
        return mOrientation;
    }
}