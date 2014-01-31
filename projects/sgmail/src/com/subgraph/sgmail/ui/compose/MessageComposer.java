package com.subgraph.sgmail.ui.compose;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.bouncycastle.openpgp.PGPException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import com.subgraph.sgmail.identity.EncryptedMultipart;
import com.subgraph.sgmail.identity.MessageEncrypter;
import com.subgraph.sgmail.identity.PublicIdentity;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.ui.MessageBodyUtils;

public class MessageComposer extends Composite {

	private final Model model;
	
	private final ComposeCloseListener closeListener;
	private final ComposerButtons buttonSection;
	private final ComposerHeader headerSection;
	
	private StyledText bodyText;
	
	MessageComposer(Composite parent, Model model, ComposeCloseListener closeListener) {
		this(parent, model, null, false, closeListener);
	}
	
	MessageComposer(Composite parent, Model model, Message replyMessage, boolean isReplyAll, ComposeCloseListener closeListener) {
		super(parent, SWT.NONE);
		this.model = model;
		this.closeListener = closeListener;
		
		setLayout(createLayout());
		
		buttonSection = new ComposerButtons(this, createSendListener(), createCancelListener());
		buttonSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		headerSection = new ComposerHeader(this, model, createHeaderValidityListener());
		headerSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		bodyText = createBody();
		
		if(replyMessage != null) {
			headerSection.populateForReply(replyMessage, isReplyAll);
			populateReply(replyMessage, isReplyAll);
			
		}
		
		buttonSection.setSendButtonEnabled(headerSection.isHeaderValid());
		createDropTarget();
	}
	
	
	private Layout createLayout() {
		final GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		return layout;
	}

	private SelectionListener createCancelListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				closeListener.closeEvent();
			}
		};
	}

	private HeaderValidityListener createHeaderValidityListener() {
		return new HeaderValidityListener() {
			@Override
			public void headerValidityEvent(boolean isValid) {
				buttonSection.setSendButtonEnabled(isValid);
			}
		};
	}

	private SelectionListener createSendListener() {
		return new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					sendMessage();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}
	
	private void populateReply(Message replyMessage, boolean isReplyAll)  {
		try {
			final String quotedBody = MessageBodyUtils.createQuotedBody(replyMessage);
			bodyText.setText(quotedBody);
			bodyText.setCaretOffset(0);
			bodyText.setFocus();
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}


	private StyledText createBody() {
		final StyledText body = new StyledText(this, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		body.setMargins(10, 10, 10, 10);
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return body;
	}
	
	private void sendMessage() throws MessagingException {
		final Message msg = headerSection.createNewMessage(headerSection.getSelectedAccount());
		sendEncryptedMessage((MimeMessage) msg);
		/*
		msg.setText(bodyText.getText());
		buttonSection.setProgressVisible(true);
		buttonSection.setProgressMessage("Sending...");
		new Thread(new SendMailTask(this, msg, headerSection.getSelectedAccount())).start();
		*/
	}
	
	
	private void sendEncryptedMessage(MimeMessage msg) throws MessagingException {
		List<PublicIdentity> ids = new ArrayList<>();
		for(Address a: msg.getAllRecipients()) {
			if(a instanceof InternetAddress) {
				InternetAddress ia = (InternetAddress) a;
				ids.addAll(model.findIdentitiesFor(ia.getAddress()));
			}
		}
		if(ids.isEmpty()) {
			System.out.println("No keys :(");
		} else {
			sendEncryptedMessage(msg, ids);
		}
	}
	
	private void sendEncryptedMessage(MimeMessage msg, List<PublicIdentity> identities) throws MessagingException {
		final EncryptedMultipart emp = new EncryptedMultipart();
		MessageEncrypter me = new MessageEncrypter();
		try {
			String body = bodyText.getText();
			emp.setBody(me.encryptMessageBody(body, identities));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (PGPException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		msg.setContent(emp);
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			msg.writeTo(out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String s = new String(out.toByteArray());
		System.out.println("final message\n"+s);
		
		
		
	}
	
	void onMailSendProgress(final String message) {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				buttonSection.setProgressMessage(message);
			}
		});
	}

	void onMailSendSuccess() {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				closeListener.closeEvent();
			}
		});
	}
	
	void onMailSendFailed(String errorMessage) {
		
	}
	
	void attachFile(File file) {
		if(file.isFile() && file.canRead()) {
			System.out.println("Adding attachment: "+ file);
		}
	}

	
	private void createDropTarget() {
		final int operations = DND.DROP_COPY | DND.DROP_MOVE;
		final DropTarget target = new DropTarget(this, operations);
		final Transfer[] transfers = new Transfer[] { FileTransfer.getInstance() };
		target.setTransfer(transfers);
		target.addDropListener(new DropTargetAdapter() {
			@Override
			public void drop(DropTargetEvent event) {
				String[] fileNames = (String []) event.data;
				for(String s: fileNames) {
					attachFile(new File(s));
				}
			}
		});
	}
}
