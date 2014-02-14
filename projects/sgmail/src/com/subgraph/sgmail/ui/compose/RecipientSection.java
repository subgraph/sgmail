package com.subgraph.sgmail.ui.compose;

import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.Subscribe;
import com.google.common.net.InternetDomainName;
import com.subgraph.sgmail.events.ContactPublicIdentityChangedEvent;
import com.subgraph.sgmail.model.Contact;
import com.subgraph.sgmail.model.Model;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecipientSection {

	final static int TYPE_TO = 0;
	final static int TYPE_CC = 1;
	final static int TYPE_BCC = 2;
	
	final static Map<Integer, String> labels = 
			ImmutableMap.of(TYPE_TO, "To", TYPE_CC, "CC", TYPE_BCC, "BCC");

    private final Model model;
    private final ComposerHeader headerSection;
	private final int type;
	private final Text textField;
	private List<Contact> contactList;
    private boolean haveKeysForAll;

	RecipientSection(Model model, ComposerHeader headerSection, int type, ModifyListener modifyListener) {
        this.model = model;
        this.headerSection = headerSection;
		this.type = type;
		createLabel(headerSection, type);
		textField = createTextField(headerSection, modifyListener);
        contactList = new ArrayList<>();
        haveKeysForAll = true;
        model.registerEventListener(this);
	}

    @Subscribe
    public void onContactPublicIdentityChanged(ContactPublicIdentityChangedEvent event) {
        haveKeysForAll = true;
        for(Contact c: contactList) {
            if(c.getPublicIdentity() == null) {
                haveKeysForAll = false;
            }
        }
        headerSection.updateRecipientKeyAvailability();
    }
	public void setText(String text) {
		textField.setText(text);
	}

	public int getType() {
		return type;
	}

	public boolean isValid() {
		final boolean allowEmpty = (type != TYPE_TO);
		return isValid(allowEmpty);
	}
	
	private boolean isValid(boolean allowEmpty) {
		String s = textField.getText().trim();
		if(s.isEmpty()) return allowEmpty;
		
		return isValidRecipient(s);
	}
	
	public InternetAddress[] getAddresses() {
		if(!isValid(false)) {
			return new InternetAddress[0];
		}
		try {
			return InternetAddress.parse(textField.getText().trim());
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new InternetAddress[0];
		}
	}

	private boolean isValidRecipient(String fieldText) {
		try {
            if(!isValidRecipientString(fieldText)) {
                return false;
            }
			return InternetAddress.parse(fieldText, false).length > 0;
		} catch (AddressException e) {
			return false;
		}
	}

    private boolean isValidRecipientString(String text) {
        for(String address: text.split(",")) {
            String parts[] = address.split("@");
            if(!( parts.length == 2 &&
                  isValidUser(parts[0]) &&
                  isValidDomain(parts[1])) ) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidUser(String user) {
        return !user.isEmpty();
    }

    private boolean isValidDomain(String domain) {
        if(!InternetDomainName.isValid(domain)) {
            return false;
        }
        InternetDomainName idn = InternetDomainName.from(domain);
        return idn.hasPublicSuffix();
    }
	
	private Label createLabel(Composite composite, int type) {
		final Label label = new Label(composite, SWT.NONE);
		label.setText(getLabelText(type));
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		return label;
	}
	
	private String getLabelText(int type) {
		if(labels.containsKey(type)) {
			return labels.get(type) + ":";
		} else {
			return "??:";
		}
	}
	
	private Text createTextField(Composite composite, ModifyListener modifyListener) {
		final Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		text.setLayoutData(gd);
		text.addModifyListener(modifyListener);
        text.addFocusListener(createFocusListener());
		return text;
	}

    private FocusListener createFocusListener() {
        return new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                populateContacts();
            }
        };
    }


    private void populateContacts() {
        contactList.clear();
        haveKeysForAll = true;
        for(InternetAddress address :getAddresses()) {
            Contact contact = model.getContactByEmailAddress(address.getAddress());
            if(contact.getPublicIdentity() == null) {
                haveKeysForAll = false;
                contact.fetchPublicIdentity();
            }
            contactList.add(contact);
        }
        headerSection.updateRecipientKeyAvailability();
    }

    List<Contact> getContactList() {
        return contactList;
    }

    boolean haveKeysForAllRecipients() {
        return haveKeysForAll;
    }
}
