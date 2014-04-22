package com.subgraph.sgmail.ui.compose;


import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.events.ContactPublicIdentityChangedEvent;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.model.Contact;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.Preferences;
import com.subgraph.sgmail.model.StoredPreferences;
import org.eclipse.swt.widgets.Display;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageCompositionState {

    private final Model model;
    private final MessageComposer composer;

    private final MimeMessage replyMessage;

    private MailAccount selectedAccount;

    private String subject;
    private Map<Message.RecipientType, List<Contact>> recipientMap = new HashMap<>();
    private boolean isSigningRequested;
    private boolean isEncryptRequested;
    private boolean recipientKeysAvailable;
    private boolean isHeaderValid;

    MessageCompositionState(Model model, MessageComposer composer, MimeMessage replyMessage) {
        this.model = model;
        this.composer = composer;
        this.replyMessage = replyMessage;
        this.model.registerEventListener(this);
    }

    @Subscribe
    public void onContactPublicIdentityChanged(ContactPublicIdentityChangedEvent event) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                updateRecipientKeysAvailable();
            }
        });

    }

    MimeMessage getReplyMessage() {
        return replyMessage;
    }

    String getSubject() {
        return subject;
    }
    public void setSelectedAccount(MailAccount account) {
        selectedAccount = Preconditions.checkNotNull(account);
        final StoredPreferences preferences = account.getPreferences();
        isSigningRequested = preferences.getBoolean(Preferences.ACCOUNT_DEFAULT_SIGN);
        isEncryptRequested = preferences.getBoolean(Preferences.ACCOUNT_DEFAULT_ENCRYPT);
        composer.updateOpenPGPButtons();
    }

    public MimeMessage createMessage(String bodyText) throws MessagingException {
        MimeMessage msg =  new MessageBuilder(this).createMessage();
        msg.setText(bodyText);
        if(isSigningRequested || isEncryptRequested) {
            OpenPGPProcessing openpgp = new OpenPGPProcessing(model, selectedAccount, msg,  isEncryptRequested, isSigningRequested);
            if(openpgp.process()) {
                return openpgp.getOutputMessage();
            }
        }
        return msg;
    }

    public void setRecipientAddresses(Message.RecipientType section, InternetAddress[] addresses) {
        final List<Contact> contacts = getContactsForSection(section);
        contacts.clear();
        for(InternetAddress addr: addresses) {
            addToContactList(contacts, addr);
        }
        updateRecipientKeysAvailable();
    }

    private void updateRecipientKeysAvailable() {
        recipientKeysAvailable = testRecipientKeysAvailable();
        composer.updateOpenPGPButtons();
    }

    private boolean testRecipientKeysAvailable() {
        boolean atLeastOneContact = false;
        for(List<Contact> contacts: recipientMap.values()) {
            if(!contacts.isEmpty()) {
                atLeastOneContact = true;
            }
            if(!allContactsHaveKeys(contacts)) {
                return false;
            }
        }
        return atLeastOneContact;
    }

    private boolean allContactsHaveKeys(List<Contact> contacts) {
        for(Contact c: contacts) {
            if(c.getPublicIdentity() == null && c.getLocalPublicKeys().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void addToContactList(List<Contact> contacts, InternetAddress address) {
        final Contact c = model.getContactByEmailAddress(address.getAddress());
        contacts.add(c);
        if(c.getPublicIdentity() == null) {
            c.fetchPublicIdentity();
        }
    }

    List<Contact> getContactsForSection(Message.RecipientType section) {
        if(!recipientMap.containsKey(section)) {
            recipientMap.put(section, new ArrayList<Contact>());
        }
        return recipientMap.get(section);
    }

    public boolean isSigningKeyAvailable() {
        return getSigningKey() != null;
    }

    public boolean areRecipientKeysAvailable() {
        return recipientKeysAvailable;
    }

    public void setEncryptionRequested(boolean value) {
        this.isEncryptRequested = value;
    }

    public void setSigningRequested(boolean value) {
        isSigningRequested = value;

    }

    public boolean isSigningRequested() {
        return isSigningRequested;
    }

    public boolean isEncryptionRequested() {
        return isEncryptRequested;
    }

    public PrivateIdentity getSigningKey() {
        if(selectedAccount == null) {
            return null;
        } else if(selectedAccount.getIdentity() != null) {
            return selectedAccount.getIdentity().getPrivateIdentity();
        } else {
            return model.findPrivateIdentity(selectedAccount.getEmailAddress());
        }
    }

    public void setIsHeaderValid(boolean isValid) {
        if(isValid != isHeaderValid) {
            isHeaderValid = isValid;
            composer.headerValidityChanged(isHeaderValid);
        }

    }

    public MailAccount getSelectedAccount() {
        return selectedAccount;
    }

    public void setSubject(String text) {
        subject = text;
    }
}
