package com.subgraph.sgmail.ui.panes.right;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.subgraph.sgmail.model.LocalMimeMessage;
import com.subgraph.sgmail.ui.ImageCache;
import com.subgraph.sgmail.ui.MessageUtils;

public class MessageHeaderViewer extends Composite {

	private static Font BOLD_FONT;
	private final Message message;
	private final Label newMessageIndicator;

	public MessageHeaderViewer(Composite parent, Message message) {
		super(parent, SWT.NONE);
		this.message = message;
		final GridLayout layout = new GridLayout(3, false);
		layout.horizontalSpacing = 0;
		setLayout(layout);
		newMessageIndicator = createNewMessageIndicatorLabel();
		updateNewMessageIndicator();
		try {
			renderMessageHeader();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void renderMessageHeader() throws MessagingException {
		
		Composite middle = new Composite(this, SWT.NONE);
		middle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		middle.setLayout(new GridLayout(2, false));
		
		Color white = getDisplay().getSystemColor(SWT.COLOR_WHITE);
		Color grey = getDisplay().getSystemColor(SWT.COLOR_GRAY);
		middle.setBackground(white);
		setBackground(white);
		
		Label right = new Label(this, SWT.NONE);
		right.setBackground(white);
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		right.setImage(getAvatarImage());
		
		Label fromLabel = new Label(middle, SWT.LEFT);
		fromLabel.setBackground(white);
		fromLabel.setFont(getBoldFont(fromLabel));
		fromLabel.setText(MessageUtils.getSender(message, true));
		fromLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label dateLabel = new Label(middle, SWT.RIGHT);
		dateLabel.setBackground(white);
		dateLabel.setForeground(grey);
		dateLabel.setText(MessageUtils.getSentDate(message));
		dateLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label toLabel = new Label(middle, SWT.LEFT);
		toLabel.setBackground(white);
		toLabel.setForeground(grey);
		toLabel.setText("To: "+ MessageUtils.getRecipient(message, true));
		toLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		Label subjectLabel = new Label(middle, SWT.LEFT | SWT.WRAP);
		subjectLabel.setBackground(white);
		subjectLabel.setText(MessageUtils.getSubject(message));
		subjectLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		Label sep = new Label(middle, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
	
	}

	private Image getAvatarImage() {
		final InternetAddress address = MessageUtils.getSenderAddress(message);
		if(address != null) {
			return ImageCache.getInstance().getAvatarImage(address.getAddress());
		}
		return ImageCache.getInstance().getDisabledImage(ImageCache.USER_IMAGE);
	}
	
	void updateNewMessageIndicator() {
		if(message instanceof LocalMimeMessage) {
			final LocalMimeMessage msg = (LocalMimeMessage) message;
			if(msg.getStoredMessage().isNewMessage()) {
				newMessageIndicator.setImage(ImageCache.getInstance().getImage(ImageCache.BLUE_DOT_IMAGE));
			} else {
				newMessageIndicator.setImage(null);
			}
		}
	}

	private Label createNewMessageIndicatorLabel() {
		final Label label = new Label(this, SWT.NONE);
		label.setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		final GridData gd = new GridData(SWT.LEFT, SWT.BEGINNING, false, false);
		gd.widthHint = 16;
		gd.heightHint = 16;
		gd.verticalIndent = 5;
		label.setLayoutData(gd);
		return label;
	}

	private Font getBoldFont(Control c) {
		if(BOLD_FONT == null) {
			FontData fd = c.getFont().getFontData()[0];
			FontData fd2 = new FontData(fd.getName(), fd.getHeight(), SWT.BOLD);
			BOLD_FONT = new Font(c.getDisplay(), fd2);
		}
		return BOLD_FONT;
	}
}
