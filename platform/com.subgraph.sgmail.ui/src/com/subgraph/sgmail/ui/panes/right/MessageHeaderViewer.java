package com.subgraph.sgmail.ui.panes.right;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.google.common.base.Charsets;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.nyms.NymsAgent;
import com.subgraph.sgmail.ui.ImageCache;
import com.subgraph.sgmail.ui.Resources;

public class MessageHeaderViewer extends Composite {
	private final static long TIME_24_HOURS = (24 * 60 * 60 * 1000);
	
	private final static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private final static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/YY");
	
	private final NymsAgent nymsAgent;
	private final StoredMessage message;
	private final Label newMessageIndicator;

	public MessageHeaderViewer(Composite parent, NymsAgent nymsAgent, StoredMessage message) {
		super(parent, SWT.NONE);
		this.nymsAgent = nymsAgent;
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

		middle.setBackground(white);
		setBackground(white);
		
		Label right = new Label(this, SWT.NONE);
		right.setBackground(white);
		right.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		right.setImage(getAvatarImage());
		
		Label fromLabel = new Label(middle, SWT.LEFT);
		fromLabel.setBackground(white);
        fromLabel.setFont(JFaceResources.getFont(Resources.FONT_SENDER));
		//fromLabel.setText(javamailUtils.getSenderText((MimeMessage) decryptedMessage, true));
        fromLabel.setText(message.getSender().getText(true));
		fromLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label dateLabel = new Label(middle, SWT.RIGHT);
		dateLabel.setBackground(white);
        dateLabel.setForeground(JFaceResources.getColorRegistry().get(Resources.COLOR_DATE_SECTION));
		//dateLabel.setText(javamailUtils.getSentDateText((MimeMessage) decryptedMessage));
        dateLabel.setText(getDateText(message));
		dateLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label toLabel = new Label(middle, SWT.LEFT);
		toLabel.setBackground(white);
        toLabel.setForeground(JFaceResources.getColorRegistry().get(Resources.COLOR_DATE_SECTION));
		//toLabel.setText("To: "+ javamailUtils.getToRecipientText((MimeMessage) decryptedMessage, true));
        toLabel.setText("To: "+ getToText(message));
		toLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		Label subjectLabel = new Label(middle, SWT.LEFT | SWT.WRAP);
		subjectLabel.setBackground(white);
		//subjectLabel.setText(javamailUtils.getSubjectText((MimeMessage) decryptedMessage));
		subjectLabel.setText(message.getSubject());
        subjectLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        HoverButton optionsButton = new HoverButton(middle, ImageCache.GEAR_IMAGE, createOptionsMenu());
        optionsButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));

		Label sep = new Label(middle, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
	
	}
	
	private String getToText(StoredMessage message) {
		List<MessageUser> toRecipients = message.getToRecipients();
		if(toRecipients.isEmpty()) {
			return "";
		}
		MessageUser to = toRecipients.get(0);
		return to.getText(true);
	}
	
	private String getDateText(StoredMessage message) {
		final long ts = message.getMessageDate() * 1000L;
		final long now = System.currentTimeMillis();
		
		if(ts == 0) {
			return "No sent date";
		}
		final Date d = new Date(ts);

		if((now - ts) < TIME_24_HOURS) {
			return timeFormat.format(d);
		} else if ((now - ts) < (2 * TIME_24_HOURS)) {
			return "Yesterday";
		} else {
			return dateFormat.format(d);
		}
	}

    private Menu createOptionsMenu() {
        final Menu menu = new Menu(getShell(), SWT.POP_UP);
        final MenuItem item = new MenuItem(menu, SWT.NONE);
        item.setText("View raw message");
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                viewRawMessage();
            }
        });
        return menu;
    }

    private void viewRawMessage() {
        Shell shell = new Shell(getDisplay(), SWT.DIALOG_TRIM);
        shell.setLayout(new FillLayout());
        Text text = new Text(shell, SWT.READ_ONLY | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        text.setFont(JFaceResources.getTextFont());
        final String rawMessage = new String(message.getRawMessageBytes(false), Charsets.US_ASCII);
        text.setText(rawMessage);
        shell.open();
    }

	private Image getAvatarImage() {
		//final InternetAddress address = javamailUtils.getSenderAddress((MimeMessage) decryptedMessage);
		MessageUser sender = message.getSender();
		if(sender != null) {
		  byte[] imageData = nymsAgent.getAvatarImage(sender.getAddress());
//			List<PublicIdentity> identities = identityManager.findPublicKeysByAddress(sender.getAddress());
			return ImageCache.getInstance().getAvatarImage(sender.getAddress(), imageData);
		}
		return ImageCache.getInstance().getDisabledImage(ImageCache.USER_IMAGE);
	}
	
	void updateNewMessageIndicator() {
		/*
		if(decryptedMessage instanceof LocalMimeMessage) {
			final LocalMimeMessage msg = (LocalMimeMessage) decryptedMessage;
            if(!msg.getStoredMessage().isFlagSet(StoredMessage.FLAG_SEEN)) {
				newMessageIndicator.setImage(ImageCache.getInstance().getImage(ImageCache.BLUE_DOT_IMAGE));
			} else {
				newMessageIndicator.setImage(null);
			}
		}
		*/
		if(!message.isFlagSet(StoredMessage.FLAG_SEEN)) {
			newMessageIndicator.setImage(ImageCache.getInstance().getImage(ImageCache.BLUE_DOT_IMAGE));
		} else {
			newMessageIndicator.setImage(null);
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
}
