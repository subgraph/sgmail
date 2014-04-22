package com.subgraph.sgmail.internal.messages;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.collections.ActivatableArrayList;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Storeable;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.messages.StoredMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class StoredFolderImpl implements StoredFolder, Activatable, Storeable {

    // This list must be an 'activatable' implementation because it will be
    // wrapped and manipulated by glazed lists
    private final ArrayList<StoredMessage> messages = new ActivatableArrayList<>();
    private final Account account;
    private String name;

    private transient EventList<StoredMessage> messageEventList;
	private transient Activator activator;
	private transient Database database;

    public StoredFolderImpl(Account account, String name) {
        this.account = checkNotNull(account);
        this.name = checkNotNull(name);
    }

    @Override
    public String getName() {
        activate(ActivationPurpose.READ);
        return name;
    }

    @Override
    public void rename(String newName) {
        activate(ActivationPurpose.WRITE);
        this.name = checkNotNull(newName);
    }

    @Override
    @SuppressWarnings("deprecation")
    public EventList<StoredMessage> getMessageEventList() {
        activate(ActivationPurpose.READ);
        synchronized (this) {
           if(messageEventList == null) {
               // This constructor is deprecated for the exact reason we need to use it
               messageEventList = new BasicEventList<>(messages);
           }
           return messageEventList;
        }
    }

    /*
    @Override
    public boolean hasNewMessages() {
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().readLock().lock();
        try {
            for(StoredMessage msg: eventList) {
                if(!msg.isFlagSet(StoredMessage.FLAG_SEEN)) {
                   return true;
                }
            }
            return false;
        } finally {
            eventList.getReadWriteLock().readLock().unlock();
        }
    }
    */

    @Override
    public int getMessageCount() {
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().readLock().lock();
        try {
            return eventList.size();
        } finally {
            eventList.getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public void addMessage(StoredMessage message) {
        for (MessageAttachment attachment : message.getAttachments()) {
            database.store(attachment);
        }
        database.store(message);
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().writeLock().lock();
        try {
            eventList.add(message);
            message.incrementReferenceCount();
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
            database.commit();
        }
    }

    @Override
    public void expungeMessages() {
        final List<StoredMessage> deletedMessages = performExpunge();
        for(StoredMessage msg: deletedMessages) {
            database.delete(msg);
        }
        database.commit();
    }

    protected List<StoredMessage> performExpunge() {
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().writeLock().lock();
        try {
            if(!hasDeletedMessages(eventList)) {
                return Collections.emptyList();
            }
            final List<StoredMessage> savedMessages = new ArrayList<>();
            final List<StoredMessage> deletedMessages = new ArrayList<>();
            sortDeletedMessages(eventList, savedMessages, deletedMessages);
            eventList.clear();
            eventList.addAll(savedMessages);
            return deletedMessages;
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
        }
    }

    private static boolean hasDeletedMessages(List<StoredMessage> sourceList) {
        for(StoredMessage msg: sourceList) {
            if(msg.isFlagSet(StoredMessage.FLAG_DELETED)) {
                return true;
            }
        }
        return false;
    }

    private static void sortDeletedMessages(List<StoredMessage> sourceList, List<StoredMessage> savedMessages, List<StoredMessage> deletedMessages) {
        for(StoredMessage msg: sourceList) {
            if(msg.isFlagSet(StoredMessage.FLAG_DELETED)) {
                deletedMessages.add(msg);
            } else {
                savedMessages.add(msg);
            }
        }
    }

    @Override
    public void clearFolder() {
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().writeLock().lock();
        try {
            for (StoredMessage msg: eventList) {
                msg.decrementReferenceCount();
            }
            eventList.clear();
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
        }
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
