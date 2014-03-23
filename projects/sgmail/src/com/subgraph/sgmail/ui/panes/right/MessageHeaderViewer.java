package com.subgraph.sgmail.ui.panes.right;

import com.google.common.base.Charsets;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.model.LocalMimeMessage;
import com.subgraph.sgmail.ui.ImageCache;
import com.subgraph.sgmail.ui.MessageUtils;
import com.subgraph.sgmail.ui.Resources;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MessageHeaderViewer extends Composite {

	private final Message rawMessage;
    private final Message decryptedMessage;
	private final Label newMessageIndicator;

	public MessageHeaderViewer(Composite parent, Message rawMessage, Message decryptedMessage) {
		super(parent, SWT.NONE);
        this.rawMessage = rawMessage;
        this.decryptedMessage = decryptedMessage;

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
		fromLabel.setText(MessageUtils.getSender(decryptedMessage, true));
		fromLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label dateLabel = new Label(middle, SWT.RIGHT);
		dateLabel.setBackground(white);
        dateLabel.setForeground(JFaceResources.getColorRegistry().get(Resources.COLOR_DATE_SECTION));
		dateLabel.setText(MessageUtils.getSentDate(decryptedMessage));
		dateLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Label toLabel = new Label(middle, SWT.LEFT);
		toLabel.setBackground(white);
        toLabel.setForeground(JFaceResources.getColorRegistry().get(Resources.COLOR_DATE_SECTION));
		toLabel.setText("To: "+ MessageUtils.getRecipient(decryptedMessage, true));
		toLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		Label subjectLabel = new Label(middle, SWT.LEFT | SWT.WRAP);
		subjectLabel.setBackground(white);
		subjectLabel.setText(MessageUtils.getSubject(decryptedMessage));
        subjectLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        HoverButton optionsButton = new HoverButton(middle, ImageCache.GEAR_IMAGE, createOptionsMenu());
        optionsButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));

		Label sep = new Label(middle, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
	
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
        text.setText(getRawMessage());
        shell.open();
    }

    private String getRawMessage() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            if(rawMessage != null) {
                rawMessage.writeTo(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        return new String(out.toByteArray(), Charsets.US_ASCII);
    }

	private Image getAvatarImage() {
		final InternetAddress address = MessageUtils.getSenderAddress(decryptedMessage);
		if(address != null) {
			return ImageCache.getInstance().getAvatarImage(address.getAddress());
		}
		return ImageCache.getInstance().getDisabledImage(ImageCache.USER_IMAGE);
	}
	
	void updateNewMessageIndicator() {
		if(decryptedMessage instanceof LocalMimeMessage) {
			final LocalMimeMessage msg = (LocalMimeMessage) decryptedMessage;
            if(!msg.getStoredMessage().isFlagSet(StoredMessage.FLAG_SEEN)) {
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
}
