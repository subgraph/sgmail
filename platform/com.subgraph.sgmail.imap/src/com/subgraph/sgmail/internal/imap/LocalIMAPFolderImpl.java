package com.subgraph.sgmail.internal.imap;

import static com.google.common.base.Preconditions.checkNotNull;
import ca.odell.glazedlists.EventList;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.google.common.collect.Lists;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Storeable;
import com.subgraph.sgmail.imap.LocalIMAPFolder;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.messages.StoredMessage;

import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;

import java.util.List;
import java.util.Set;

public class LocalIMAPFolderImpl implements LocalIMAPFolder, Storeable, Activatable {

    private final IMAPAccountImpl account;
    private final StoredFolder storageFolder;
    private long uidValidity;
    private long uidNext;
    private long highestModSeq;
    private TLongArrayList uidMap = new TLongArrayList();
    
	private transient Database database;
	private transient Activator activator;

    public LocalIMAPFolderImpl(IMAPAccountImpl account, StoredFolder storageFolder) {
        this.account = account;
        this.storageFolder = storageFolder;
    }

    @Override
    public long getUIDValidity() {
        activate(ActivationPurpose.READ);
        return uidValidity;
    }

    @Override
    public void setUIDValidity(long value) {
        activate(ActivationPurpose.WRITE);
        this.uidValidity = value;
    }

    @Override
    public long getUIDNext() {
        activate(ActivationPurpose.READ);
        return uidNext;
    }

    @Override
    public void setUIDNext(long value) {
        activate(ActivationPurpose.WRITE);
        this.uidNext = value;
    }

    @Override
    public long getHighestModSeq() {
        activate(ActivationPurpose.READ);
        return highestModSeq;
    }

    @Override
    public void setHighestModSeq(long value) {
        activate(ActivationPurpose.WRITE);
        this.highestModSeq = value;
    }

    @Override
    public TLongList getUIDMap() {
        activate(ActivationPurpose.READ);
        return uidMap;
    }

    @Override
    public void clearFolder() {
        activate(ActivationPurpose.WRITE);
        uidNext = uidValidity = highestModSeq = 0;
        uidMap.clear();;
        storageFolder.clearFolder();
    }

    @Override
	public void commit() {
		database.commit();
	}

	@Override
    public StoredMessage getMessageByMessageNumber(int number) {
        activate(ActivationPurpose.READ);
        final EventList<StoredMessage> eventList = storageFolder.getMessageEventList();
        eventList.getReadWriteLock().readLock().lock();
        try {
            if(number < 1 || number > eventList.size()) {
                throw new IllegalArgumentException("Invalid message number " + number + " folder size: "+ eventList.size());
            }
            return eventList.get(number - 1);
        } finally {
            eventList.getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public void appendMessage(StoredMessage message, long messageUID) {
        activate(ActivationPurpose.WRITE);
        final EventList<StoredMessage> eventList = storageFolder.getMessageEventList();
        eventList.getReadWriteLock().writeLock().lock();
        try {
            storageFolder.addMessage(message);
            uidMap.add(messageUID);
            account.getMailAccount().addMessage(message);
            database.commit();
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
        }
    }

    @Override
    public int getMessageCount() {
        activate(ActivationPurpose.READ);
        return uidMap.size();
    }

    @Override
    public void expungeMessagesByUID(Set<Long> uids) {
        activate(ActivationPurpose.WRITE);
        final EventList<StoredMessage> eventList = storageFolder.getMessageEventList();
        final List<StoredMessage> savedMessages = Lists.newArrayList();
        final List<StoredMessage> removedMessages = Lists.newArrayList();
        final TLongList savedUIDMap = new TLongArrayList();

        eventList.getReadWriteLock().writeLock().lock();
        try {
            sortExpungedMessages(uids, eventList, savedUIDMap, savedMessages, removedMessages);
            uidMap.clear(savedUIDMap.size());
            uidMap.addAll(savedUIDMap);
            eventList.clear();
            eventList.addAll(savedMessages);
            for (StoredMessage msg: removedMessages) {
                msg.decrementReferenceCount();
            }
            account.getMailAccount().removeDeletedMessages();
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
        }
    }

    private void sortExpungedMessages(Set<Long> expungeUIDs, List<StoredMessage> sourceList, TLongList savedUIDMap, List<StoredMessage> savedMessages, List<StoredMessage> removedMessages) {
        for(int i = 0; i < sourceList.size(); i++) {
            final StoredMessage msg = sourceList.get(i);
            Long uid = uidMap.get(i);
            if(expungeUIDs.contains(uid)) {
                removedMessages.add(msg);
            } else {
                savedMessages.add(msg);
                savedUIDMap.add(uid);
            }
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
