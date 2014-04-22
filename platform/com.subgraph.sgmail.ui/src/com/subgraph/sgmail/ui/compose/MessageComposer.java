package com.subgraph.sgmail.ui.compose;

import com.google.common.base.Splitter;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.identity.IdentityManager;
import com.subgraph.sgmail.messages.MessageUser;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.openpgp.MessageProcessor;

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

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

public class MessageComposer extends Composite {

    private final MessageCompositionState state;
	
	private final ComposeCloseListener closeListener;
	private final ComposerButtons buttonSection;
	private final ComposerHeader headerSection;
	private StyledText bodyText;
	
	MessageComposer(Composite parent, JavamailUtils javamailUtils, IEventBus eventBus, MessageProcessor messageProcessor, IdentityManager identityManager, Model model, ComposeCloseListener closeListener) {
		this(parent, javamailUtils, eventBus, messageProcessor, identityManager, model, null, false, closeListener);
	}
	
	MessageComposer(Composite parent, JavamailUtils javamailUtils, IEventBus eventBus, MessageProcessor messageProcessor, IdentityManager identityManager, Model model, StoredMessage replyMessage, boolean isReplyAll, ComposeCloseListener closeListener) {
		super(parent, SWT.NONE);
        this.state = new MessageCompositionState(eventBus, messageProcessor, identityManager, this, replyMessage);
		this.closeListener = closeListener;
		setLayout(createLayout());
		
		buttonSection = new ComposerButtons(this, createSendListener(), createCancelListener());
		buttonSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		headerSection = new ComposerHeader(this, state, model, javamailUtils);
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
	
	private void populateReply(StoredMessage replyMessage, boolean isReplyAll)  {
		//final String quotedBody = javamailUtils.createQuotedBody((MimeMessage) replyMessage);
		final String quotedBody = createQuotedBody(replyMessage);
		bodyText.setText(quotedBody);
		bodyText.setCaretOffset(0);
		bodyText.setFocus();
	}

	public static String createQuotedBody(StoredMessage message) {
		final StringBuilder sb = new StringBuilder();
		//final String body = getTextBody(message);
		final String body = message.getBodyText();
		final String replyLine = createReplyLine(message);
		sb.append('\n');
		sb.append(replyLine);
		for(String line: Splitter.on('\n').split(body)) {
			if(line.startsWith(">")) {
				sb.append(">");
			} else {
				sb.append("> ");
			}
			sb.append(line);
			sb.append('\n');
		}
		return sb.toString();
	}

	private final static DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG);
	private final static DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.LONG);
	
	private static String createReplyLine(StoredMessage message) {
		
		final MessageUser sender = message.getSender();
		
		//final String sender = MessageUtils.getSender(message, false);
		if(message.getMessageDate() == 0) {
			return sender.getText(false) + " wrote:\n";
		} else {
			final Date sent = new Date(message.getMessageDate() * 1000L);
			synchronized(dateFormat) {
				return "On "+ dateFormat.format(sent) +" at "+ timeFormat.format(sent) +", "+ sender.getText(false) + " wrote:\n";
			}
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

	private void transmitMessage(MimeMessage message, MailAccount account) {
		buttonSection.setProgressVisible(true);
		buttonSection.setProgressMessage("Sending...");
		new Thread(new SendMailTask(this, message, account)).start();
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
