package io.monke.app.internal.views.flow;

import android.graphics.Point;

import androidx.recyclerview.widget.RecyclerView;

public class FlowLayoutHelper {
    RecyclerView.LayoutManager layoutManager;
    RecyclerView recyclerView;

    public FlowLayoutHelper(RecyclerView.LayoutManager layoutManager, RecyclerView recyclerView) {
        this.layoutManager = layoutManager;
        this.recyclerView = recyclerView;
    }

    public static boolean hasItemsPerLineLimit(FlowLayoutManager.FlowLayoutOptions layoutOptions) {
        return layoutOptions.itemsPerLine > 0;
    }

    public static boolean shouldStartNewline(int x, int childWidth, int leftEdge, int rightEdge, FlowLayoutContext FlowLayoutContext) {
        if (hasItemsPerLineLimit(FlowLayoutContext.layoutOptions) && FlowLayoutContext.currentLineItemCount == FlowLayoutContext.layoutOptions.itemsPerLine) {
            return true;
        }
        switch (FlowLayoutContext.layoutOptions.alignment) {
            case RIGHT:
                return x - childWidth < leftEdge;
            case LEFT:
            default:
                return x + childWidth > rightEdge;
        }
    }

    public int leftVisibleEdge() {
        return recyclerView.getPaddingLeft();
    }

    public int rightVisibleEdge() {
        return layoutManager.getWidth() - layoutManager.getPaddingRight();
    }

    public int visibleAreaWidth() {
        return rightVisibleEdge() - leftVisibleEdge();
    }

    public int topVisibleEdge() {
        return layoutManager.getPaddingTop();
    }

    public int bottomVisibleEdge() {
        return layoutManager.getHeight() - layoutManager.getPaddingBottom();
    }

    public Point layoutStartPoint(FlowLayoutContext FlowLayoutContext) {
        switch (FlowLayoutContext.layoutOptions.alignment) {
            case RIGHT:
                return new Point(rightVisibleEdge(), topVisibleEdge());
            default:
                return new Point(leftVisibleEdge(), topVisibleEdge());
        }
    }
}
