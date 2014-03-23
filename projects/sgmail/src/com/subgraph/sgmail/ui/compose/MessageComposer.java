package com.subgraph.sgmail.ui.compose;

import com.google.common.base.Charsets;
import com.subgraph.sgmail.accounts.IMAPAccount;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.ui.MessageBodyUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MessageComposer extends Composite {

	private final Model model;
    private final MessageCompositionState state;
	
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
        this.state = new MessageCompositionState(model, this, (MimeMessage)replyMessage);
		this.closeListener = closeListener;
		setLayout(createLayout());
		
		buttonSection = new ComposerButtons(this, createSendListener(), createCancelListener());
		buttonSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		headerSection = new ComposerHeader(this, model, state);
		headerSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		bodyText = createBody();
		
		if(replyMessage != null) {
			headerSection.populateForReply(replyMessage, isReplyAll);
			populateReply(replyMessage, isReplyAll);
			
		}
		
		buttonSection.setSendButtonEnabled(headerSection.isHeaderValid());
        headerSection.updateOpenPGPButtons();
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
        final MimeMessage msg = state.createMessage(bodyText.getText());
        transmitMessage(msg, state.getSelectedAccount());
	}

	private void transmitMessage(MimeMessage message, IMAPAccount account) {
		buttonSection.setProgressVisible(true);
		buttonSection.setProgressMessage("Sending...");
		new Thread(new SendMailTask(this, message, account)).start();
	}
	
	private void dumpMessage(MimeMessage msg) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			msg.writeTo(output);
			System.out.println(new String(output.toByteArray(), Charsets.US_ASCII));
		} catch (IOException | MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

    public void updateOpenPGPButtons() {
        if(headerSection != null) {
            headerSection.updateOpenPGPButtons();
        }
    }

    public void headerValidityChanged(boolean isHeaderValid) {
        buttonSection.setSendButtonEnabled(isHeaderValid);
    }
}
