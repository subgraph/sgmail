package com.subgraph.sgmail.ui.compose;

import com.google.common.base.Strings;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.model.*;
import com.subgraph.sgmail.ui.ImageCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.*;
import java.util.List;

public class ComposerHeader extends Composite {

	private final Map<Integer, RecipientSection> recipientSectionMap = new HashMap<>();
	private final Model model;
	private final HeaderValidityListener validityListener;
	private final List<IMAPAccount> accounts;
	private final Text subjectText;
	private final Combo fromCombo;
	
	private final Button signingButton;
	private final Button encryptingButton;
	
	private boolean encryptRequested;
	private boolean signRequested;
	private boolean recipientKeysAvailable;
	
	private Message replyMessage;
	
	ComposerHeader(Composite parent, Model model, HeaderValidityListener validityListener) {
		super(parent, SWT.NONE);
		setLayout(createLayout());
		this.model = model;
		this.validityListener = validityListener;
		accounts = getAccountList(model);
		createSeparator();
		final ModifyListener modifyListener = createModifyListener();
		
		addRecipientSection(RecipientSection.TYPE_TO, modifyListener);
		addRecipientSection(RecipientSection.TYPE_CC, modifyListener);
		
		subjectText = createSubjectText();
		fromCombo = createFromCombo(accounts);
		signingButton = createButton();
		encryptingButton = createButton();
		
		createSeparator();
		
		onSelectedAccountChanged();
		updateButtonStates();
	}


	public Message createNewMessage(final IMAPAccount account) throws MessagingException {
		final Session session = Session.getInstance(new Properties());
		final MimeMessage msg = new MimeMessage(session) {
		    protected void updateMessageID() throws MessagingException {
		    	setHeader("Message-ID", generateMessageID(account)); 
		    }
		};
		
		addRecipients(msg, RecipientSection.TYPE_TO, RecipientType.TO);
		addRecipients(msg, RecipientSection.TYPE_CC, RecipientType.CC);
		addRecipients(msg, RecipientSection.TYPE_BCC, RecipientType.BCC);
		
		msg.setSubject(getSubject());
		msg.setFrom(getFromAddress());
		
		if(replyMessage != null) {
			configureNewMessageForReply(msg, (MimeMessage) replyMessage);;
		}
		
		return msg;
	}
	
	private void configureNewMessageForReply(MimeMessage newMessage, MimeMessage replyMessage) throws MessagingException {
		final String msgId = replyMessage.getMessageID();
		if(msgId != null) {
			newMessage.setHeader("In-Reply-To", msgId);
		}
		final String refs = getReferences(replyMessage, msgId);
		if(refs != null) {
			newMessage.setHeader("References", MimeUtility.fold(12, refs));
		}
	}
	
	final String getReferences(MimeMessage msg, String messageId) throws MessagingException {
		String refs = msg.getHeader("References", " ");
		if(refs == null) {
			refs = msg.getHeader("In-Reply-To", " ");
		}
		if(messageId != null) {
			if(refs != null) {
				refs = MimeUtility.unfold(refs) + " " + messageId;
			} else {
				refs = messageId;
			}
		}
		return refs;
	}

	private String generateMessageID(IMAPAccount account) {
		final UUID uuid = UUID.randomUUID();
		if(account == null) {
			return "<"+ uuid + ">";
		} else {
			return "<"+ uuid + "@" + account.getDomain() + ">";
		}
	}

	private void addRecipients(Message msg, int sectionType, RecipientType rt) throws MessagingException {
		final RecipientSection section = recipientSectionMap.get(sectionType);
		if(section == null) {
			return;
		}
		final InternetAddress[] addresses = section.getAddresses();
		if(addresses == null || addresses.length == 0) {
			return;
		}
		msg.addRecipients(rt, addresses);
	}
	
	private Address getFromAddress() throws AddressException {
		final IMAPAccount account = getSelectedAccount();
		return new InternetAddress(account.getEmailAddress(), true);
	}

	public void populateForReply(Message message, boolean isReplyAll) {
		replyMessage = message;
		final RecipientSection toSection = recipientSectionMap.get(RecipientSection.TYPE_TO);
		
		try {
			if(toSection != null) {
				toSection.setText(getReplyTo(message));
			}
			subjectText.setText(getReplySubject(message));
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	private static String getReplyTo(Message message) throws MessagingException {
		final Address[] replyTo = message.getReplyTo();
		if(replyTo == null || !(replyTo[0] instanceof InternetAddress)) {
			return "";
		}
		final InternetAddress address = (InternetAddress) replyTo[0];
		return address.getAddress();
	}

	private static String getReplySubject(Message message) throws MessagingException {
		final String subject = Strings.nullToEmpty(message.getSubject());
		if(subject.startsWith("Re: ")) {
			return subject;
		} else {
			return "Re: "+ subject;
		}
	}

	public IMAPAccount getSelectedAccount() {
		return accounts.get(fromCombo.getSelectionIndex());
	}
	
	public boolean isSigningRequested() {
		return signRequested;
	}
	
	public boolean isEncryptionRequested() {
		return encryptRequested;
	}

	public String getSubject() {
		return subjectText.getText();
	}

	private Layout createLayout() {
		final GridLayout layout = new GridLayout(4, false);
		
		return layout;
	}

	private List<IMAPAccount> getAccountList(Model model) {
		final List<IMAPAccount> accounts = new ArrayList<IMAPAccount>();
		for(Account a: model.getAccounts()) {
			if(a instanceof IMAPAccount) {
				accounts.add((IMAPAccount) a);
			}
		}
		return accounts;
	}

	private void addRecipientSection(int type, ModifyListener modifyListener) {
		recipientSectionMap.put(type, new RecipientSection(model, this, type, modifyListener));
	}

    void updateRecipientKeyAvailability() {
        recipientKeysAvailable = false;
        for(RecipientSection rs: recipientSectionMap.values()) {
            if(!rs.haveKeysForAllRecipients()) {
                return;
            }
        }
        recipientKeysAvailable = true;
        updateButtonStates();
    }

	private ModifyListener createModifyListener() {
		return new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				validate();
			}
		};
	}

	private Text createSubjectText() {
		createLabel("Subject:");
		final Text text = new Text(this, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		return text;
	}

	private Combo createFromCombo(List<IMAPAccount> accounts) {
		createLabel("From:");
		
		final Combo combo = new Combo(this, SWT.READ_ONLY);
		for(IMAPAccount a: accounts) {
			combo.add(a.getRealname() + " <"+ a.getEmailAddress() + ">");
		}
		combo.select(0);
		
		combo.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false));
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				onSelectedAccountChanged();
			}
		});
		return combo;	
	}
	
	
	private void onSelectedAccountChanged() {
		final IMAPAccount selectedAccount = getSelectedAccount();
		if(selectedAccount == null) {
			signRequested = false;
			encryptRequested = false;
		} else {
			final StoredAccountPreferences preferences = selectedAccount.getPreferences();
			signRequested = preferences.getBoolean(Preferences.ACCOUNT_DEFAULT_SIGN);
			encryptRequested = preferences.getBoolean(Preferences.ACCOUNT_DEFAULT_ENCRYPT);
		}
	}

	private PrivateIdentity getSigningKeyForAccount(IMAPAccount account) {
		if(account == null) {
			return null;
		} else if(account.getIdentity() != null) {
			return account.getIdentity().getPrivateIdentity();
		}
		return model.findPrivateIdentity(account.getEmailAddress());
	}

	private Label createLabel(String s) {
		final Label label = new Label(this, SWT.NONE);
		label.setText(s);
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		return label;
	}
	
	private Label createSeparator() {
		final Label sep = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		sep.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		return sep;
	}
	
	private Button createButton() {
		final Button button = new Button(this, SWT.PUSH);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				onClicked(button);
			}
		});
		return button;
	}
	
	private void onClicked(Button button) {
		if(button == signingButton) {
			if(isSigningKeyAvailable()) {
				signRequested = !signRequested;
			} 
		} else if(button == encryptingButton) {
			if(recipientKeysAvailable) {
				encryptRequested = !encryptRequested;
			}
		}
		updateButtonStates();
	}
	
	private void updateButtonStates() {
		updateSigningButton();
		updateEncryptionButton();
	}
	
	private boolean isSigningKeyAvailable() {
		return getSigningKeyForAccount(getSelectedAccount()) != null;
	}

	private void updateSigningButton() {
		if(!isSigningKeyAvailable()) {
			setSigningButtonImage(false);
			signingButton.setToolTipText("No identity available for signing");
		} else if(signRequested) {
			setSigningButtonImage(true);
			signingButton.setToolTipText("This message will be signed");
		} else {
			setSigningButtonImage(false);
			signingButton.setToolTipText("Signing disabled for this message");
		}
	}
	
	private void updateEncryptionButton() {
		if(!recipientKeysAvailable) {
			setEncryptionButtonImage(false, encryptRequested);
			encryptingButton.setToolTipText("No encryption keys available for recipients");
		} else if(encryptRequested) {
			setEncryptionButtonImage(true, true);
			encryptingButton.setToolTipText("This message will be encrypted");
		} else {
			setEncryptionButtonImage(false, false);
			encryptingButton.setToolTipText("This message will not be encrypted");
		}
	}
	
	private void setSigningButtonImage(boolean enabled) {
		final String key = ImageCache.SIGNED_IMAGE;
		final ImageCache cache = ImageCache.getInstance();
		final Image image = (enabled) ? (cache.getImage(key)) : (cache.getDisabledImage(key));
		signingButton.setImage(image);
		signingButton.pack(true);
	}
	
	private void setEncryptionButtonImage(boolean enabled, boolean isLocked) {
		final String key = (isLocked) ? (ImageCache.LOCKED_IMAGE) : (ImageCache.UNLOCKED_IMAGE);
		final ImageCache cache = ImageCache.getInstance();
		final Image image = (enabled) ? (cache.getImage(key)) : (cache.getDisabledImage(key));
		encryptingButton.setImage(image);
	}
	
	private void validate() {
		validityListener.headerValidityEvent(isHeaderValid());
	}
	
	public boolean isHeaderValid() {

		for(RecipientSection r: recipientSectionMap.values()) {
			if(!r.isValid()) {
				return false;
			}
		}
		return true;
	}


}
