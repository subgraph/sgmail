package com.subgraph.sgmail.internal.accounts;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.collections.ActivatableArrayList;
import com.db4o.ta.Activatable;
import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.accounts.ServerDetails;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Preferences;
import com.subgraph.sgmail.database.Storeable;
import com.subgraph.sgmail.internal.messages.StoredFolderImpl;
import com.subgraph.sgmail.internal.messages.StoredMessageLabelCollectionImpl;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.messages.StoredMessageLabel;
import com.subgraph.sgmail.messages.StoredMessageLabelCollection;

import gnu.trove.map.hash.TIntObjectHashMap;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;


public class BasicMailAccount implements MailAccount, Storeable, Activatable {

    private final ServerDetails smtpAccount;
    private final String emailAddress;
    private final StoredMessageLabelCollection labelCollection;

    //private final List<StoredMessage> allMessages = new ActivatableArrayList<>();
    private final ArrayList<StoredMessage> allMessages = new ArrayList<>();
    private final List<StoredFolder> folders = new ActivatableArrayList<>();
    private final Preferences preferences;
    private final TIntObjectHashMap<StoredMessage> messagesById = new TIntObjectHashMap<>();

    private String realname;
    private String label;

    private transient EventList<StoredMessage> messageEventList;
    private transient EventList<StoredMessage> readOnlyEventList;
    private transient PropertyChangeSupport propertyChangeSupport;
    private transient Activator activator;
    private transient Database database;
    


    public BasicMailAccount(String label, String emailAddress, String realName, ServerDetails smtpServer, Preferences accountPreferences) {
        this.label = checkNotNull(label);
        this.emailAddress = checkNotNull(emailAddress);
        this.realname = realName;
        this.smtpAccount = checkNotNull(smtpServer);
        this.preferences = accountPreferences;
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
        database.store(newFolder);
        folders.add(newFolder);
        database.commit();
        getPropertyChangeSupport().firePropertyChange("folders", null, null);
        return newFolder;
    }

    @Override
    public Preferences getPreferences() {
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

    public void addMessages(Collection<StoredMessage> messages) {
      System.out.println("addMessages called with "+ messages.size() + " messages");

        try {
            final List<StoredMessage> messageList = writeLockMessageEventList();
            for(StoredMessage sm: messages) {
              if(!messagesById.contains(sm.getMessageId())) {
                messageList.add(sm);
                messagesById.put(sm.getMessageId(), sm);
              }
            }
            database.store(messagesById);
        } finally {
            writeUnlockMessageEventList();
        }
        getPropertyChangeSupport().firePropertyChange("allMessages", null, null);
    }

    public void addMessage(StoredMessage message) {
        try {
            final List<StoredMessage> messageList = writeLockMessageEventList();
            if(!messagesById.contains(message.getMessageId())) {
              messageList.add(message);
              messagesById.put(message.getMessageId(), message);
              database.store(messagesById);
            }
        } finally {
            writeUnlockMessageEventList();
            database.commit();
        }
        getPropertyChangeSupport().firePropertyChange("allMessages", null, null);
    }

    @Override
    public void removeDeletedMessages() {
        final EventList<StoredMessage> eventList = getWritableMessageEventList();
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
                    database.delete(msg);
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
        final EventList<StoredMessage> eventList = getWritableMessageEventList();
        eventList.getReadWriteLock().writeLock().lock();
        return eventList;
    }

    private void writeUnlockMessageEventList() {
        database.store(allMessages);
        getWritableMessageEventList().getReadWriteLock().writeLock().unlock();
    }

    public EventList<StoredMessage> getMessageEventList() {
      synchronized(allMessages) {
        if(readOnlyEventList == null) {
          readOnlyEventList = GlazedLists.readOnlyList(getWritableMessageEventList());
        }
        return readOnlyEventList;
      }
    }
    
    @SuppressWarnings("deprecation")
    private EventList<StoredMessage> getWritableMessageEventList() {
      activate(ActivationPurpose.READ);
      synchronized(allMessages) {
        if(messageEventList == null) {
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
    
	@Override
	public void activate(ActivationPurpose activationPurpose) {
		if(activator != null) {
			activator.activate(activationPurpose);
		}
	}

	@Override
	public void bind(Activator activator) {
		if(this.activator == activator) {
			return;
		}
		if(activator != null  && this.activator != null) {
			throw new IllegalStateException("Object can only be bound one to an activator");
		}
		this.activator = activator;
	}

	@Override
	public void setDatabase(Database database) {
		this.database = checkNotNull(database);

	}
}
