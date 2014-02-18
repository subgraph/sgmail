package com.subgraph.sgmail.ui.panes.right;

import com.subgraph.sgmail.ui.ImageCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

public class HoverButton extends Composite {

    private final StackLayout stack;
    private final Label label;
    private final Button button;

    public HoverButton(Composite parent, String imageName, final Menu popupMenu) {
        super(parent, SWT.NONE);
        stack = new StackLayout();
        setLayout(stack);

        label = new Label(this, SWT.CENTER);
        label.setImage(ImageCache.getInstance().getDisabledImage(imageName));
        label.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        label.addMouseTrackListener(createLabelMouseTrackListener());
        
        button = new Button(this, SWT.FLAT);
        button.setImage(ImageCache.getInstance().getImage(imageName));
        button.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        button.addMouseTrackListener(createButtonMouseTrackListener());

        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                popupMenu.setVisible(true);
            }
        });

        stack.topControl = label;

        layout();

    }

    private MouseTrackListener createLabelMouseTrackListener() {
        return new MouseTrackAdapter() {
            @Override
            public void mouseEnter(MouseEvent e) {
                stack.topControl = button;
                layout();
            }

        };
    }
        private MouseTrackListener createButtonMouseTrackListener() {
        return new MouseTrackAdapter() {
            @Override
            public void mouseExit(MouseEvent e) {
                stack.topControl = label;
                layout();
            }
        };
    }

}
