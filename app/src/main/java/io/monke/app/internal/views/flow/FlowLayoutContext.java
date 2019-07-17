package io.monke.app.internal.views.flow;

public class FlowLayoutContext {
    public FlowLayoutManager.FlowLayoutOptions layoutOptions;
    public int currentLineItemCount;

    public static FlowLayoutContext clone(FlowLayoutContext FlowLayoutContext) {
        FlowLayoutContext resultContext = new FlowLayoutContext();
        resultContext.currentLineItemCount = FlowLayoutContext.currentLineItemCount;
        resultContext.layoutOptions = FlowLayoutManager.FlowLayoutOptions.clone(FlowLayoutContext.layoutOptions);
        return resultContext;
    }

    public static FlowLayoutContext fromLayoutOptions(FlowLayoutManager.FlowLayoutOptions layoutOptions) {
        FlowLayoutContext FlowLayoutContext = new FlowLayoutContext();
        FlowLayoutContext.layoutOptions = layoutOptions;
        return FlowLayoutContext;
    }
}
