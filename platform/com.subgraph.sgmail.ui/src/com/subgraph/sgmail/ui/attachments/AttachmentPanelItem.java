package com.subgraph.sgmail.ui.attachments;

import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.ui.ImageCache;
import com.subgraph.sgmail.ui.Resources;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class AttachmentPanelItem extends Composite {
    private final AttachmentPanel attachmentPanel;
    private final MessageAttachment attachment;
    private final Color originalBackground;

    private volatile String savedToPath;
    private volatile String errorMessage;

    public AttachmentPanelItem(Composite parent, AttachmentPanel attachmentPanel, MessageAttachment attachment) {
        super(parent, SWT.NONE);
        this.attachmentPanel = attachmentPanel;
        this.attachment = attachment;
        this.originalBackground = getBackground();
        setLayout(new GridLayout());

        final MouseListener listener = createMouseListener();
        createImageLabel(this, attachment).addMouseListener(listener);
        createSizeLabel(this, attachment.getFileLength()).addMouseListener(listener);
        createFilenameLabel(this, attachment.getFilename()).addMouseListener(listener);
        addMouseListener(listener);
    }

    private Label createImageLabel(Composite parent, MessageAttachment attachment) {
        final Label imageLabel = new Label(parent, SWT.NONE);
        final Image image = ImageCache.getInstance().getMimetypeIcon(attachment.getMimeSubType());
        imageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        imageLabel.setImage(image);
        return imageLabel;
    }

    private Label createSizeLabel(Composite parent, long size) {
        final Label sizeLabel = new Label(parent, SWT.CENTER);
        sizeLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        sizeLabel.setText(ByteSizeFormatter.formatByteCount(size));
        return sizeLabel;
    }

    private Label createFilenameLabel(Composite parent, String filename) {
        final Label filenameLabel = new Label(parent, SWT.WRAP);
        filenameLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        filenameLabel.setText(filename);
        return filenameLabel;
    }

    private MouseListener createMouseListener() {
        return new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                setSelected();
            }
        };
    }

    void setSavedToPath(String path) {
        this.savedToPath = path;
        this.errorMessage = null;
    }

    void setErrorMessage(String message) {
        this.savedToPath = null;
        this.errorMessage = message;

    }

    String getSavedToPath() {
        return savedToPath;
    }

    String getErrorMessage() {
        return errorMessage;
    }

    void setUnselected() {
        setBackground(originalBackground);
    }

    void setSelected() {
        setBackground(JFaceResources.getColorRegistry().get(Resources.COLOR_ATTACHMENT_PANEL_HIGHLIGHT));
        attachmentPanel.setItemSelected(this);
    }

    MessageAttachment getAttachment() {
        return attachment;
    }
}
