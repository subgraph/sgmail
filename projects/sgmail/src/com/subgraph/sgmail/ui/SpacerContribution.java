package com.subgraph.sgmail.ui;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * A toolbar contribution item which will consume all remaining toolbar width.  Used to right justify
 * toolbar controls such as a search box.
 */
public class SpacerContribution extends ContributionItem {

    private final int marginWidth;
    private ToolItem toolItem;
    private int index;

    public SpacerContribution() {
        this(0);
    }

    /**
     *
     * @param marginWidth Value is subtracted from width of spacer in order to create a margin between last item and right side of toolbar.
     */
    public SpacerContribution(int marginWidth) {
        super("spacer");
        this.marginWidth = marginWidth;
        toolItem = null;
        index = -1;
    }


    public final void fill(ToolBar toolbar, int index) {
        if(toolItem != null) {
            throw new IllegalStateException("ToolItem already created");
        }
        this.index = index;

        toolItem = new ToolItem(toolbar, SWT.SEPARATOR, index);
        toolItem.setControl(new Label(toolbar, SWT.NONE));
        toolItem.setWidth(computeWidth());
        toolbar.getParent().addControlListener(createControlListener());
    }

    private int computeWidth() {
        final int toolbarWidth = toolItem.getParent().getBounds().width;
        final int itemsWidth = calculateOtherItemsWidth();
        final int width = (toolbarWidth - itemsWidth) - marginWidth;
        return (width >= 0) ? (width) : (0);
    }

    private int calculateOtherItemsWidth() {
        int width = 0;
        final ToolBar tb = toolItem.getParent();
        final int count = tb.getItemCount();
        for(int i = 0; i < count; i++) {
            if(i != index) {
                width += tb.getItem(i).getBounds().width;

            }
        }
        return width;
    }

    private ControlListener createControlListener() {
        return new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                if(toolItem != null) {
                    toolItem.setWidth(computeWidth());
                }
            }
        };
    }
}
