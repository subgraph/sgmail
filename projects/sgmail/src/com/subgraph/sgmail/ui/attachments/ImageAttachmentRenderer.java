package com.subgraph.sgmail.ui.attachments;

import com.subgraph.sgmail.attachments.StoredMessageAttachmentExtractor;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.StoredIMAPMessage;
import com.subgraph.sgmail.ui.Resources;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;

import java.io.InputStream;

public class ImageAttachmentRenderer extends Composite {

    private final static int MARGIN_WIDTH = 20;
    private final static int MARGIN_HEIGHT = 30;
    private final static int HEADER_SPACING = 10;

    public static ImageAttachmentRenderer createForAttachment(Composite parent, StoredIMAPMessage message, MessageAttachment attachment) {
        final StoredMessageAttachmentExtractor extractor = new StoredMessageAttachmentExtractor(message);
        try {
            final InputStream stream = extractor.extractAttachment(attachment);
            final Image image = new Image(parent.getDisplay(), new ImageData(stream));
            return new ImageAttachmentRenderer(parent, image, attachment.getFilename());
        } catch (StoredMessageAttachmentExtractor.AttachmentExtractionException e) {
            e.printStackTrace();
        }
        return null;
    }

    private final Image image;
    private final String label;
    private final Point labelSize;

    public ImageAttachmentRenderer(Composite parent, Image image, String label) {
        super(parent, SWT.NONE);
        this.image = image;
        this.label = "  "+ label + "  ";
        GC gc = new GC(getDisplay());
        gc.setFont(JFaceResources.getFont(Resources.FONT_HEADER));
        this.labelSize = gc.textExtent(label);
        gc.dispose();
        setBackground(JFaceResources.getColorRegistry().get(Resources.COLOR_WHITE));
        addDisposeListener(e -> {
            if(image != null) {
                image.dispose();
            }
        });
        addPaintListener(e -> {
            paintHeader(e.gc);
            paintImage(e.gc);
        });
        addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e) {
                getParent().layout();
            }
        });
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        final int clientWidth = getClientArea().width;
        final int imageWidth = image.getImageData().width;
        final int imageHeight = image.getImageData().height;
        final int extraHeight = labelSize.y + HEADER_SPACING + 2 * MARGIN_HEIGHT;

        if(clientWidth == 0) {
            return new Point(imageWidth, imageHeight + extraHeight);
        } else if (clientWidth > imageWidth) {
            return new Point(clientWidth, imageHeight + extraHeight);
        } else {
            int scaledHeight = imageHeight * clientWidth / imageWidth;
            return new Point(clientWidth, scaledHeight + extraHeight);
        }
    }

    private void paintHeader(GC gc) {
        final int width = getClientArea().width - (MARGIN_WIDTH * 2);
        final int textX = MARGIN_WIDTH + (width / 2) - (labelSize.x / 2);
        int y = MARGIN_HEIGHT + (labelSize.y / 2);
        int x = MARGIN_WIDTH;
        gc.setForeground(JFaceResources.getColorRegistry().get(Resources.COLOR_HEADER));
        gc.drawLine(x, y, x + width, y);
        gc.setFont(JFaceResources.getFont(Resources.FONT_HEADER));
        gc.setAntialias(SWT.ON);
        gc.setTextAntialias(SWT.ON);
        gc.drawText(label, textX, MARGIN_HEIGHT);
    }

    private void paintImage(GC gc) {
        int y = MARGIN_HEIGHT + HEADER_SPACING + gc.textExtent(label).y;
        final Rectangle clientArea = getClientArea();
        final Rectangle imageBounds = image.getBounds();
        if(clientArea.width >= imageBounds.width) {
            gc.drawImage(image, 0, 0, imageBounds.width, imageBounds.height, MARGIN_WIDTH, y, imageBounds.width, imageBounds.height);
            return;
        }
        int scaledHeight = imageBounds.height * clientArea.width / imageBounds.width;
        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.HIGH);
        gc.drawImage(image, 0, 0, imageBounds.width, imageBounds.height, MARGIN_WIDTH, y, clientArea.width, scaledHeight);
    }
}
