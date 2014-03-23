package com.subgraph.sgmail.accounts.impl;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;
import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.accounts.IMAPAccount;
import com.subgraph.sgmail.accounts.SMTPAccount;
import com.subgraph.sgmail.messages.StoredIMAPFolder;
import com.subgraph.sgmail.messages.StoredMessages;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.StoredAccountPreferences;
import com.sun.mail.gimap.GmailMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.logging.Logger;

public class IMAPAccountImpl extends AbstractMailAccount implements IMAPAccount {

	private final static Logger logger = Logger.getLogger(IMAPAccountImpl.class.getName());
	
	private final static int DEFAULT_IMAPS_PORT = 993;
	private final static int DEFAULT_IMAP_PORT = 143;

    public static class Builder extends AbstractMailAccount.Builder {
        public Builder smtpAccount(SMTPAccount value) { super.smtpAccount(value); return this; }
        public Builder emailAddress(String value) { super.emailAddress(value); return this; }
        public Builder realname(String value) { super.realname(value); return this; }
        public Builder login(String value) { super.login(value); return this; }
        public Builder password(String value) { super.password(value);  return this; }
        public Builder label(String value) { super.label(value); return this; }
        public Builder hostname(String value) { super.hostname(value); return this; }
        public Builder port(int value) { super.port(value); return this; }
        public Builder onion(String value) { super.onion(value); return this; }
        public IMAPAccount build() {
            return new IMAPAccountImpl(this);
        }
    }

	private boolean isSecure;
	private boolean isAutomaticSyncEnabled;
	private List<StoredIMAPFolder> folders = new ActivatableArrayList<>();

	private final StoredAccountPreferences preferences;


    protected IMAPAccountImpl(Builder builder) {
        super(builder);
        this.isSecure = true;
        this.isAutomaticSyncEnabled = true;
        this.preferences = StoredAccountPreferences.create(this);
    }

	public Model getModel() {
		return model;
	}
	
	public StoredAccountPreferences getPreferences() {
		activate(ActivationPurpose.READ);
		return preferences;
	}

	public void setAutomaticSyncEnabled(boolean value) {
		activate(ActivationPurpose.WRITE);
		isAutomaticSyncEnabled = value;
	}

	public boolean isAutomaticSyncEnabled() {
		activate(ActivationPurpose.READ);
		return isAutomaticSyncEnabled;
	}

	public List<StoredIMAPFolder> getFolders() {
		activate(ActivationPurpose.READ);
		synchronized (folders) {
			return ImmutableList.copyOf(folders);
		}
	}

	public StoredIMAPFolder getFolderByName(String name) {
		activate(ActivationPurpose.READ);
		synchronized (folders) {
			final StoredIMAPFolder folder = findFolderByName(name);
			return (folder != null) ? (folder) : (createNewFolder(name));
		}
	}
	
	private StoredIMAPFolder findFolderByName(String name) {
        activate(ActivationPurpose.READ);
		for(StoredIMAPFolder f: folders) {
			if(f.getName().equals(name)) {
				return f;
			}
		}
		return null;
	}
	
	private StoredIMAPFolder createNewFolder(String name) {
        activate(ActivationPurpose.READ);
        final StoredIMAPFolder newFolder = StoredMessages.createIMAPFolder(this, name);
        model.store(newFolder);
		folders.add(newFolder);
        getPropertyChangeSupport().firePropertyChange("folders", null, null);
		return newFolder;
	}

	public boolean isSecure() {
		activate(ActivationPurpose.READ);
		return isSecure;
	}

    public boolean isGmailAccount() {
        return AccountUtils.isGmailServerAddress(getHostname());
    }

	protected int getDefaultPort() {
		return isSecure ? DEFAULT_IMAPS_PORT : DEFAULT_IMAP_PORT;
	}


	protected String getProtocol() {
        if(isGmailAccount()) {
            return (isSecure) ? "gimaps" : "gimap";
        } else {
            return (isSecure) ? "imaps" : "imap";
        }
	}

    public long generateConversationIdForMessage(MimeMessage message) throws MessagingException {
        if(message instanceof GmailMessage)  {
            return ((GmailMessage) message).getThrId();
        }
        throw new UnsupportedOperationException("Generation of conversation id for regular IMAP messages not yet implemented");
    }


    public long generateUniqueMessageIdForMessage(MimeMessage message) throws MessagingException {
        if(message instanceof GmailMessage) {
            return ((GmailMessage) message).getMsgId();
        }
        throw new UnsupportedOperationException("Generation of unique message id for regular IMAP messages not yet implemented");
    }

}
