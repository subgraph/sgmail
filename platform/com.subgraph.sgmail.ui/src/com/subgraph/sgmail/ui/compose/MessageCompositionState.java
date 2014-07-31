package com.subgraph.sgmail.ui.compose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.common.base.Preconditions;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.database.Contact;
import com.subgraph.sgmail.database.ContactManager;
import com.subgraph.sgmail.database.Preferences;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.nyms.NymsAgent;
import com.subgraph.sgmail.nyms.NymsAgentException;
import com.subgraph.sgmail.nyms.NymsOutgoingProcessingResult;

public class MessageCompositionState {
  private final static Logger logger = Logger.getLogger(MessageCompositionState.class.getName());

  private final NymsAgent nymsAgent;
  private final ContactManager contactManager;

  private final MessageComposer composer;

  private final StoredMessage replyMessage;

  private MailAccount selectedAccount;

  private String subject;
  private Map<Message.RecipientType, List<Contact>> recipientMap = new HashMap<>();
  private boolean isSigningRequested;
  private boolean isEncryptRequested;
  private boolean recipientKeysAvailable;
  private boolean isHeaderValid;

  MessageCompositionState(IEventBus eventBus, NymsAgent nymsAgent, ContactManager contactManager, MessageComposer composer, StoredMessage replyMessage) {
    this.nymsAgent = nymsAgent;
    this.contactManager = contactManager;

    this.composer = composer;
    this.replyMessage = replyMessage;
    eventBus.register(this);
  }

  /*
  @Subscribe
  public void onContactPublicIdentityChanged(ContactPublicIdentityChangedEvent event) {
    Display.getDefault().asyncExec(new Runnable() {
      @Override
      public void run() {
        updateRecipientKeysAvailable();
      }
    });

  }
  */

  StoredMessage getReplyMessage() {
    return replyMessage;
  }

  String getSubject() {
    return subject;
  }

  public void setSelectedAccount(MailAccount account) {
    selectedAccount = Preconditions.checkNotNull(account);
    final Preferences preferences = account.getPreferences();
    isSigningRequested = preferences.getBoolean(Preferences.ACCOUNT_DEFAULT_SIGN);
    isEncryptRequested = preferences.getBoolean(Preferences.ACCOUNT_DEFAULT_ENCRYPT);
    composer.updateOpenPGPButtons();
  }

  public MimeMessage createMessage(String bodyText) throws MessagingException {
    MimeMessage msg = new MessageBuilder(this).createMessage();
    msg.setText(bodyText);
    if (isSigningRequested || isEncryptRequested) {
      try {
        NymsOutgoingProcessingResult result = nymsAgent.processOutgoingMessage(msg, isSigningRequested, isEncryptRequested, "");
        if(result.getProcessedMessage() != null) {
          return result.getProcessedMessage();
        }
      } catch (NymsAgentException e) {
        logger.warning("Nyms agent processing failed: "+ e.getMessage());
      }
    }
    return msg;
  }

  public void setRecipientAddresses(Message.RecipientType section, InternetAddress[] addresses) {
    final List<Contact> contacts = getContactsForSection(section);
    contacts.clear();
    for (InternetAddress addr : addresses) {
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
    for (List<Contact> contacts : recipientMap.values()) {
      if (!contacts.isEmpty()) {
        atLeastOneContact = true;
      }
      if (!allContactsHaveKeys(contacts)) {
        return false;
      }
    }
    return atLeastOneContact;
  }

  private boolean allContactsHaveKeys(List<Contact> contacts) {
    for(Contact c: contacts) {
      try {
        if(!nymsAgent.hasKeyForAddress(c.getEmailAddress())) {
          return false;
        }
      } catch (NymsAgentException e) {
        logger.warning("Exception making request to nyms agent: "+ e.getMessage());
        return false;
      }
    }
    return true;

    /*
     * for(Contact c: contacts) { if(c.getPublicIdentity() == null &&
     * c.getLocalPublicKeys().isEmpty()) { return false; } } return true;
     */
  }

  private void addToContactList(List<Contact> contacts, InternetAddress address) {
    final Contact c = contactManager.getContactByEmail(address.getAddress());
    contacts.add(c);
    /*
     * if(c.getPublicIdentity() == null) { c.fetchPublicIdentity(); }
     */
  }

  List<Contact> getContactsForSection(Message.RecipientType section) {
    if (!recipientMap.containsKey(section)) {
      recipientMap.put(section, new ArrayList<Contact>());
    }
    return recipientMap.get(section);
  }

  
  public boolean isSigningKeyAvailable() {
    if(selectedAccount == null) {
      return false;
    }
    try {
      return nymsAgent.hasSigningKey(selectedAccount.getEmailAddress());
    } catch (NymsAgentException e) {
      logger.warning("Exception making request to nyms agent: "+ e.getMessage());
      return false;
    }
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

  /*
  public PrivateIdentity getSigningKey() {
    if (selectedAccount == null) {
      return null;
    } else if (selectedAccount.getIdentity() != null) {
      return selectedAccount.getIdentity();
    } else {
      return identityManager.findPrivateKeyByAddress(selectedAccount.getEmailAddress());
    }
  }
  */

  public void setIsHeaderValid(boolean isValid) {
    if (isValid != isHeaderValid) {
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
