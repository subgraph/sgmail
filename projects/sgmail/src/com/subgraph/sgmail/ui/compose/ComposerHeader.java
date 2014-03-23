package com.subgraph.sgmail.ui.compose;

import com.google.common.base.Strings;
import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.accounts.IMAPAccount;
import com.subgraph.sgmail.model.Model;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComposerHeader extends Composite {

	private final Map<RecipientType, RecipientSection> recipientSectionMap = new HashMap<>();

	private final List<IMAPAccount> accounts;
	private final Text subjectText;
	private final Combo fromCombo;

    private final MessageCompositionState state;
    private final OpenPGPButtons pgpButtons;
	
	ComposerHeader(Composite parent, Model model , MessageCompositionState state) {
		super(parent, SWT.NONE);
		setLayout(createLayout());
        this.state = state;
		accounts = getAccountList(model);
		createSeparator();
		final ModifyListener modifyListener = createModifyListener();
		
		addRecipientSection(RecipientType.TO, modifyListener);
		addRecipientSection(RecipientType.CC, modifyListener);

		
		subjectText = createSubjectText();
		fromCombo = createFromCombo(accounts);
        pgpButtons = new OpenPGPButtons(this, state);

		createSeparator();
		
		onSelectedAccountChanged();
	}

	public void populateForReply(Message message, boolean isReplyAll) {
        final RecipientSection toSection = recipientSectionMap.get(RecipientType.TO);
		
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

	private Layout createLayout() {
		final GridLayout layout = new GridLayout(4, false);
		
		return layout;
	}

	private List<IMAPAccount> getAccountList(Model model) {
		final List<IMAPAccount> accounts = new ArrayList<>();
		for(Account a: model.getAccountList().getAccounts()) {
			if(a instanceof IMAPAccount) {
				accounts.add((IMAPAccount) a);
			}
		}
		return accounts;
	}

	private void addRecipientSection(RecipientType type, ModifyListener modifyListener) {
		recipientSectionMap.put(type, new RecipientSection(state, this, type, modifyListener));
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
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                state.setSubject(subjectText.getText());
            }
        });
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
        state.setSelectedAccount(selectedAccount);

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

	private void validate() {
        state.setIsHeaderValid(isHeaderValid());
	}
	
	public boolean isHeaderValid() {

		for(RecipientSection r: recipientSectionMap.values()) {
			if(!r.isValid()) {
				return false;
			}
		}
		return true;
	}


    public void updateOpenPGPButtons() {
        pgpButtons.updateButtons();
    }
}
