package com.subgraph.sgmail.ui.panes.right;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.events.MessageStateChangedEvent;
import com.subgraph.sgmail.model.LocalMimeMessage;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.Preferences;
import com.subgraph.sgmail.model.StoredMessage;

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
	private final Message message;
	private final MessageHeaderViewer headerViewer;
	private volatile boolean isHighlighted;
	
	
	static {
		final ColorRegistry cr = JFaceResources.getColorRegistry();
		RGB rgb = new RGB(0x7d, 0xbb, 0xfd);
		cr.put("highlight", rgb);
	}
	
	public MessageViewer(Composite parent, final RightPane pane, Message message, Model model) {
		super(parent, SWT.NONE);
		final GridLayout layout = new GridLayout();
		layout.verticalSpacing = 0;
		layout.marginHeight = layout.marginWidth = MARGIN_SIZE;
		setLayout(layout);
		this.message = message;
		this.model = model;
		
		headerViewer = new MessageHeaderViewer(this, message);
		headerViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		MessageBodyViewer body = new MessageBodyViewer(this, message);
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
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
		if(!(message instanceof LocalMimeMessage)) {
			return;
		}
		if(((LocalMimeMessage) message).getStoredMessage() == event.getMessage()) {
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
		return message;
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
			message.writeTo(out);
			System.out.println(new String(out.toByteArray(), Charsets.UTF_8));
		} catch (IOException | MessagingException e) {
			logger.warning("exception dumping message: "+ e);
		}
	}
	private void markMessageSeen() {
		if(!(message instanceof LocalMimeMessage)) {
			return;
		}
		final StoredMessage sm = ((LocalMimeMessage) message).getStoredMessage();
		if(sm.isNewMessage()) {
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
