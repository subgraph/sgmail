package com.subgraph.sgmail.accounts.impl;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;
import com.subgraph.sgmail.accounts.AuthenticationCredentials;
import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.accounts.PasswordAuthenticationCredentials;
import com.subgraph.sgmail.accounts.SMTPAccount;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.messages.StoredMessageLabel;
import com.subgraph.sgmail.messages.StoredMessageLabelCollection;
import com.subgraph.sgmail.messages.StoredMessages;
import com.subgraph.sgmail.model.AbstractActivatable;
import com.subgraph.sgmail.model.Identity;
import com.subgraph.sgmail.model.Preferences;

import javax.mail.NoSuchProviderException;
import javax.mail.Store;
import javax.mail.URLName;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


public abstract class AbstractMailAccount extends AbstractActivatable implements MailAccount {
    private final static Logger logger = Logger.getLogger(AbstractMailAccount.class.getName());

    private final SMTPAccount smtpAccount;
    private final String emailAddress;
    private final StoredMessageLabelCollection labelCollection;

    private final List<StoredMessage> allMessages = new ActivatableArrayList<>();


    private Identity identity;
    private String realname;
    private String label;
    private String hostname;
    private int port;
    private String onionHostname;
    private AuthenticationCredentials authenticationCredentials;

    private transient Store remoteStore;
    private transient EventList<StoredMessage> messageEventList;
    private transient PropertyChangeSupport propertyChangeSupport;


    protected AbstractMailAccount(Builder builder) {
        smtpAccount = checkNotNull(builder.smtpAccount);
        emailAddress = checkNotNull(builder.emailAddress);
        label = checkNotNull(builder.label);
        hostname = checkNotNull(builder.hostname);
        port = builder.port;
        checkArgument(port > 0 && port <= 0xFFFF, "Port value must be between 1 and 65535");
        onionHostname = builder.onionHostname;
        authenticationCredentials = new PasswordAuthenticationCredentialsImpl(checkNotNull(builder.login), checkNotNull(builder.password));
        realname = builder.realname;
        labelCollection = StoredMessages.createLabelCollection(this);
    }

    @Override
    public String getLabel() {
        activate(ActivationPurpose.READ);
        return label;
    }

    @Override
    public List<StoredMessageLabel> getMessageLabels() {
        activate(ActivationPurpose.READ);
        return labelCollection.getLabels();
    }

    @Override
    public StoredMessageLabel getMessageLabelByName(String name) {
        activate(ActivationPurpose.READ);
        synchronized (labelCollection) {
            final StoredMessageLabel label = labelCollection.getLabelByName(name);
            if(label != null) {
                return label;
            }
            final StoredMessageLabel newLabel = labelCollection.createNewLabel(name);
            getPropertyChangeSupport().firePropertyChange("labelCollection", null, null);
            return newLabel;
        }
    }

    @Override
    public SMTPAccount getSMTPAccount() {
        activate(ActivationPurpose.READ);
        return smtpAccount;
    }

    @Override
    public synchronized Store getRemoteStore() {
        if(remoteStore == null) {
            remoteStore = createRemoteStore();
        }
        return remoteStore;
    }

    private Store createRemoteStore() {
        final URLName urlname = getURLName();
        try {
            return model.getSession().getStore(urlname);
        } catch (NoSuchProviderException e) {
            logger.warning("Could not create store for "+ urlname + " : "+ e);
            return null;
        }
    }

    @Override
    public String getEmailAddress() {
        activate(ActivationPurpose.READ);
        return emailAddress;
    }

    @Override
    public String getDomain() {
        final String[] parts = getEmailAddress().split("@");
        if(parts.length != 2) {
            throw new IllegalArgumentException("Malformed email address: "+ getEmailAddress());
        }
        return parts[1];
    }

    @Override
    public String getRealname() {
        activate(ActivationPurpose.READ);
        return realname;
    }

    @Override
    public AuthenticationCredentials getAuthenticationCredentials() {
        activate(ActivationPurpose.READ);
        return authenticationCredentials;
    }

    @Override
    public String getHostname() {
        activate(ActivationPurpose.READ);
        return hostname;
    }

    @Override
    public String getOnionHostname() {
        activate(ActivationPurpose.READ);
        return onionHostname;
    }

    @Override
    public int getPort() {
        activate(ActivationPurpose.READ);
        return port;
    }

    protected abstract int getDefaultPort();
    protected abstract String getProtocol();

    @Override
    public URLName getURLName() {
        activate(ActivationPurpose.READ);
        final String login = getPasswordAuth().getLogin();
        final String password = getPasswordAuth().getPassword();
        final String connectHostname = getConnectHostname();
        final int portValue = (port == getDefaultPort()) ? -1 : port;
        return new URLName(getProtocol(), connectHostname, portValue, null, login, password);
    }

    private String getConnectHostname() {
        if(onionHostname != null && model.getRootStoredPreferences().getBoolean(Preferences.TOR_ENABLED)) {
            return onionHostname;
        } else {
            return hostname;
        }
    }

    private PasswordAuthenticationCredentials getPasswordAuth() {
        if(!(authenticationCredentials instanceof PasswordAuthenticationCredentials)) {
            throw new IllegalStateException("Authentication credentials are not password credentials");
        }
        return (PasswordAuthenticationCredentials) authenticationCredentials;
    }

    @Override
    public void setIdentity(Identity identity) {
        activate(ActivationPurpose.WRITE);
        final Identity oldIdentity = this.identity;
        this.identity = identity;
        getPropertyChangeSupport().firePropertyChange("identity", oldIdentity, identity);
    }

    @Override
    public Identity getIdentity() {
        activate(ActivationPurpose.READ);
        return identity;
    }

    public void addMessages(Collection<StoredMessage> messages) {
        try {
            writeLockMessageEventList().addAll(messages);
        } finally {
            writeUnlockMessageEventList();
        }
        getPropertyChangeSupport().firePropertyChange("allMessages", null, null);
    }

    public void addMessage(StoredMessage message) {
        try {
            writeLockMessageEventList().add(message);
        } finally {
            writeUnlockMessageEventList();
        }
        getPropertyChangeSupport().firePropertyChange("allMessages", null, null);
    }

    public void removeMessage(StoredMessage message) {
        try {
            writeLockMessageEventList().remove(message);
        } finally {
            writeUnlockMessageEventList();
        }
        getPropertyChangeSupport().firePropertyChange("allMessages", null, null);
    }

    public void removeMessages(Collection<StoredMessage> messages) {
        try {
            writeLockMessageEventList().removeAll(messages);
        } finally {
            writeUnlockMessageEventList();
        }
        getPropertyChangeSupport().firePropertyChange("allMessages", null, null);
    }

    private EventList<StoredMessage> writeLockMessageEventList() {
        activate(ActivationPurpose.WRITE);
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().writeLock().lock();
        return eventList;
    }

    private void writeUnlockMessageEventList() {
        getMessageEventList().getReadWriteLock().writeLock().unlock();
    }

    @SuppressWarnings("deprecation")
    public EventList<StoredMessage> getMessageEventList() {
        activate(ActivationPurpose.READ);
        synchronized (allMessages) {
            if(messageEventList == null) {
                // Use deprecated contructor because we don't want to persist the glazed list object
                messageEventList = new BasicEventList<>(allMessages);
            }
            return messageEventList;
        }
    }

    protected synchronized PropertyChangeSupport getPropertyChangeSupport() {
        if(propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return propertyChangeSupport;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().removePropertyChangeListener(listener);
    }

    public static class Builder {
        private SMTPAccount smtpAccount;
        private String emailAddress;
        private String realname;
        private String login;
        private String password;
        private String label;
        private String hostname;
        private int port;
        private String onionHostname;

        public Builder smtpAccount(SMTPAccount value) { smtpAccount = value; return this; }
        public Builder emailAddress(String value) { emailAddress = value; return this; }
        public Builder realname(String value) { realname = value; return this; }
        public Builder login(String value) { login = value; return this; }
        public Builder password(String value) { password = value; return this; }
        public Builder label(String value) { label = value; return this; }
        public Builder hostname(String value) { hostname = value; return this; }
        public Builder port(int value) { port = value; return this; }
        public Builder onion(String value) { onionHostname = value; return this; }

    }
}
