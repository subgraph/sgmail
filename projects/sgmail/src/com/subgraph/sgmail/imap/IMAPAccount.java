package com.subgraph.sgmail.imap;

import com.db4o.activation.ActivationPurpose;
import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.accounts.ServerDetails;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.model.AbstractActivatable;
import com.sun.mail.gimap.GmailMessage;
import com.sun.mail.imap.IMAPStore;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.hash.TLongIntHashMap;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMAPAccount extends AbstractActivatable {
    private final MailAccount account;
    private final ServerDetails imapServerDetails;


    private int currentConversationId = 1;
    private TLongIntMap googleConversationMap = new TLongIntHashMap();
    private TLongIntMap googleMessageIdMap = new TLongIntHashMap();
    private Map<String, LocalIMAPFolder> folderMap = new HashMap<>();

    private transient IMAPStore cachedStore;

    public IMAPAccount(MailAccount account, ServerDetails imapServerDetails) {
        this.account = account;
        this.imapServerDetails = imapServerDetails;
    }

    public synchronized IMAPStore getRemoteStore() {
        activate(ActivationPurpose.READ);
        if(cachedStore == null) {
            cachedStore = (IMAPStore) imapServerDetails.createRemoteStore();
        }
        return cachedStore;
    }

    public ServerDetails getIMAPServerDetails() {
        activate(ActivationPurpose.READ);
        return imapServerDetails;
    }

    private final static List<String> GMAIL_DOMAINS = ImmutableList.of("gmail.com", "googlemail.com");

    public boolean isGmailAccount() {
        activate(ActivationPurpose.READ);
        final String hostname = imapServerDetails.getHostname();
        for(String domain: GMAIL_DOMAINS) {
            if(hostname.toLowerCase().endsWith(domain)) {
                return true;
            }
        }
        return false;
    }

    public LocalIMAPFolder getFolderByName(String name) {
        activate(ActivationPurpose.READ);
        synchronized (folderMap) {
            if(!folderMap.containsKey(name)) {
                activate(ActivationPurpose.WRITE);
                final StoredFolder folder = account.getFolderByName(name);
                final LocalIMAPFolder localFolder = new LocalIMAPFolder(this, folder);
                model.store(localFolder);
                folderMap.put(name, localFolder);
                model.commit();
            }
            return folderMap.get(name);
        }
    }

    public MailAccount getMailAccount() {
        activate(ActivationPurpose.READ);
        return account;
    }

    public synchronized int generateConversationIdForMessage(MimeMessage message) throws MessagingException {
        activate(ActivationPurpose.READ);
        if(message instanceof GmailMessage) {
            long threadId = ((GmailMessage) message).getThrId();
            if(!googleConversationMap.containsKey(threadId)) {
                activate(ActivationPurpose.WRITE);
                googleConversationMap.put(threadId, currentConversationId);
                currentConversationId += 1;
                model.commit();
            }
            return googleConversationMap.get(threadId);
        }
        throw new UnsupportedOperationException("Generation of conversation id only supported for gmail for now");
    }

    public synchronized StoredMessage getMessageForMimeMessage(MimeMessage message) throws MessagingException {
        activate(ActivationPurpose.READ);
        if(!(message instanceof GmailMessage)) {
            return null;
        }
        long id = ((GmailMessage) message).getMsgId();
        if(!googleMessageIdMap.containsKey(id)) {
            return null;
        }
        return account.getMessageById(googleMessageIdMap.get(id));
    }

    public synchronized int generateUniqueMessageIdForMessage(MimeMessage message) throws MessagingException {
        activate(ActivationPurpose.READ);
        if(message instanceof GmailMessage) {
            long messageId = ((GmailMessage) message).getMsgId();
            if (!googleMessageIdMap.containsKey(messageId)) {
                activate(ActivationPurpose.WRITE);
                googleMessageIdMap.put(messageId, model.getUniqueId());
                model.commit();
            }
            return googleMessageIdMap.get(messageId);
        }
        throw new UnsupportedOperationException("Generation of message id only supported for gmail for now");
    }

    public boolean isAutomaticSyncEnabled() {
        return true;
    }


}
