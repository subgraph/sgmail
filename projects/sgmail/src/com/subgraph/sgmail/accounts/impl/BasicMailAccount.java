package com.subgraph.sgmail.accounts.impl;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;
import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.accounts.ServerDetails;
import com.subgraph.sgmail.messages.*;
import com.subgraph.sgmail.messages.impl.StoredFolderImpl;
import com.subgraph.sgmail.messages.impl.StoredMessageLabelCollectionImpl;
import com.subgraph.sgmail.model.AbstractActivatable;
import com.subgraph.sgmail.model.Identity;
import com.subgraph.sgmail.model.StoredAccountPreferences;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;


public class BasicMailAccount extends AbstractActivatable implements MailAccount {

    private final ServerDetails smtpAccount;
    private final String emailAddress;
    private final StoredMessageLabelCollection labelCollection;

    private final List<StoredMessage> allMessages = new ActivatableArrayList<>();
    private final List<StoredFolder> folders = new ActivatableArrayList<>();
    private final StoredAccountPreferences preferences;
    private final TIntObjectHashMap<StoredMessage> messagesById = new TIntObjectHashMap<>();


    private Identity identity;
    private String realname;
    private String label;

    private transient EventList<StoredMessage> messageEventList;
    private transient PropertyChangeSupport propertyChangeSupport;


    public BasicMailAccount(String label, String emailAddress, String realName, ServerDetails smtpServer) {
        this.label = checkNotNull(label);
        this.emailAddress = checkNotNull(emailAddress);
        this.realname = realName;
        this.smtpAccount = checkNotNull(smtpServer);
        this.preferences = StoredAccountPreferences.create(this);
        labelCollection = new StoredMessageLabelCollectionImpl(this);
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
    public List<StoredFolder> getFolders() {
        activate(ActivationPurpose.READ);
        synchronized (folders) {
            return ImmutableList.copyOf(folders);
        }
    }

    @Override
    public StoredFolder getFolderByName(String name) {
        activate(ActivationPurpose.READ);
        synchronized (folders) {
            final StoredFolder folder = findFolderByName(name);
            return (folder != null) ? (folder) : (createNewFolder(name));
        }
    }

    private StoredFolder findFolderByName(String name) {
        for (StoredFolder folder : folders) {
            if(folder.getName().equals(name)) {
                return folder;
            }
        }
        return null;
    }

    private StoredFolder createNewFolder(String name) {
        activate(ActivationPurpose.READ);
        final StoredFolder newFolder = new StoredFolderImpl(this, name);
        model.store(newFolder);
        folders.add(newFolder);
        model.commit();
        getPropertyChangeSupport().firePropertyChange("folders", null, null);
        return newFolder;
    }

    @Override
    public StoredAccountPreferences getPreferences() {
        activate(ActivationPurpose.READ);
        return preferences;
    }

    @Override
    public ServerDetails getSMTPAccount() {
        activate(ActivationPurpose.READ);
        return smtpAccount;
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
            for(StoredMessage sm: messages) {
                messagesById.put(sm.getMessageId(), sm);
            }
        } finally {
            writeUnlockMessageEventList();
        }
        getPropertyChangeSupport().firePropertyChange("allMessages", null, null);
    }

    public void addMessage(StoredMessage message) {
        try {
            writeLockMessageEventList().add(message);
            messagesById.put(message.getMessageId(), message);
        } finally {
            writeUnlockMessageEventList();
            model.commit();
        }
        getPropertyChangeSupport().firePropertyChange("allMessages", null, null);
    }

    @Override
    public void removeDeletedMessages() {
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().writeLock().lock();
        try {
            final int count = countDeletedMessages(eventList);
            if(count == 0) {
                return;
            }
            final List<StoredMessage> retainedMessages = new ArrayList<>(eventList.size() - count);
            for (StoredMessage msg : eventList) {
                if(msg.getReferenceCount() > 0) {
                    retainedMessages.add(msg);
                } else {
                    model.delete(msg);
                }
            }
            eventList.clear();
            messagesById.clear();
            addMessages(retainedMessages);
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
        }
    }

    private int countDeletedMessages(List<StoredMessage> messages) {
        int count = 0;
        for (StoredMessage m : messages) {
            if(m.getReferenceCount() == 0) {
                count += 1;
            }
        }
        return count;
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

    @Override
    public StoredMessage getMessageById(int messageId) {
        getMessageEventList().getReadWriteLock().readLock().lock();
        try {
            return messagesById.get(messageId);
        } finally {
            getMessageEventList().getReadWriteLock().readLock().unlock();
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
}
