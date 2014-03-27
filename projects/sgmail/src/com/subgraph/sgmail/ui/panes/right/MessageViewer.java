package com.subgraph.sgmail.ui.panes.right;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.events.MessageStateChangedEvent;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.StoredIMAPMessage;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.model.LocalMimeMessage;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.Preferences;
import com.subgraph.sgmail.ui.attachments.AttachmentPanel;
import com.subgraph.sgmail.ui.attachments.ImageAttachmentRenderer;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class MessageViewer extends Composite {
	private final static Logger logger = Logger.getLogger(MessageViewer.class.getName());
	private final static int MARGIN_SIZE = 4;
	private final static int[] alphaValues = createAlphaValues();
	
	private static int[] createAlphaValues() {
		final int[] values = new int[MARGIN_SIZE];
		for(int i = MARGIN_SIZE - 1, a = 255; i >= 0; i--) {
			values[i] = a; a = a * 75 / 100;
		}
		return values;
	}

	private final Model model;
    private final Message rawMessage;
    private final Message decryptedMessage;
	private final MessageHeaderViewer headerViewer;
    private final MessageBodyViewer bodyViewer;
    private final AttachmentPanel attachmentPanel;
	private volatile boolean isHighlighted;
	
	
	static {
		final ColorRegistry cr = JFaceResources.getColorRegistry();
		RGB rgb = new RGB(0x7d, 0xbb, 0xfd);
		cr.put("highlight", rgb);
	}
	
	public MessageViewer(Composite parent, final RightPane pane, StoredMessage message, Message raw, Message decrypted, Model model) {
		super(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		layout.marginHeight = layout.marginWidth = MARGIN_SIZE;
		setLayout(layout);
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        this.rawMessage = raw;
        this.decryptedMessage = decrypted;
		this.model = model;
		
		headerViewer = new MessageHeaderViewer(this, rawMessage, decryptedMessage);
		headerViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		bodyViewer = new MessageBodyViewer(this, decryptedMessage);
		bodyViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        for (MessageAttachment attachment : message.getAttachments()) {
            if(attachment.getMimePrimaryType().equalsIgnoreCase("image")) {
                ImageAttachmentRenderer renderer = ImageAttachmentRenderer.createForAttachment(this, (StoredIMAPMessage) message, attachment);
                if(renderer != null) {
                    final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
                    gd.heightHint = 200;
                    renderer.setLayoutData(gd);
                }
            }
        }
        if(message.getAttachments().isEmpty()) {
            attachmentPanel = null;
        } else {
            attachmentPanel = new AttachmentPanel(model.getExecutor(), message, this);
            attachmentPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        }
		addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                onPaintControl(e);
            }
        });
		
		addAllMouseListener(this, new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				pane.selectMessageViewer(MessageViewer.this);
			}
		});
	}
	
	@Subscribe
	public void onMessageStateChanged(MessageStateChangedEvent event) {
		if(!(decryptedMessage instanceof LocalMimeMessage)) {
			return;
		}
		if(((LocalMimeMessage) decryptedMessage).getStoredMessage() == event.getMessage()) {
			updateHeaderViewer();
		}
	}
	
	private void updateHeaderViewer() {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				headerViewer.updateNewMessageIndicator();
			}
		});
	}
	
	public Message getMessage() {
		return decryptedMessage;
	}

    public void highlightTerms(List<String> terms) {
        bodyViewer.highlightTerms(terms);
    }

	private static void addAllMouseListener(Composite c, MouseListener listener) {
		c.addMouseListener(listener);
		for(Control cc: c.getChildren()) {
			if(cc instanceof Composite) {
				addAllMouseListener((Composite) cc, listener);
			} else {
				cc.addMouseListener(listener);
			}
		}
	}

	public void setHighlighted(boolean value) {
		if(value) {
			markMessageSeen();
			maybeDumpMessage();
		} else {
            if(attachmentPanel != null) {
                attachmentPanel.unselectAll();
            }
        }
		isHighlighted = value;
		redraw();
	}

	private void maybeDumpMessage() {
		if(!model.getRootStoredPreferences().getBoolean(Preferences.DUMP_SELECTED_MESSAGE)) {
			return;
		}
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			rawMessage.writeTo(out);
			System.out.println(new String(out.toByteArray(), Charsets.UTF_8));
		} catch (IOException | MessagingException e) {
			logger.warning("exception dumping message: "+ e);
		}
	}
	private void markMessageSeen() {
		if(!(decryptedMessage instanceof LocalMimeMessage)) {
			return;
		}
		final StoredIMAPMessage sm = ((LocalMimeMessage) decryptedMessage).getStoredMessage();
        if(!sm.isFlagSet(StoredMessage.FLAG_SEEN)) {
            sm.addFlag(StoredMessage.FLAG_SEEN);
			model.commit();
			headerViewer.updateNewMessageIndicator();
			model.postEvent(new MessageStateChangedEvent(sm));
		}
	}

	private void onPaintControl(PaintEvent e) {
		if(isHighlighted) {
			highlightViewer(e.gc);
		}
	}
	
	private void highlightViewer(GC gc) {
		Rectangle area = getClientArea();
		for(int i = 0; i < MARGIN_SIZE; i++) {
			drawBox(gc, area, i);
		}
	}

	private void drawBox(GC gc, Rectangle area, int offset) {
		gc.setForeground(JFaceResources.getColorRegistry().get("highlight"));
		gc.setAlpha(alphaValues[offset]);
		final int left = area.x + offset;
		final int top = area.y + offset;
		final int right = area.x + area.width - offset - 1;
		final int bottom = area.y + area.height - offset - 1;
		
		gc.drawLine(left, top, right, top);
        gc.drawLine(right, top, right, bottom);
		gc.drawLine(left, bottom, right, bottom);
		gc.drawLine(left, bottom, left, top);

	}
}
