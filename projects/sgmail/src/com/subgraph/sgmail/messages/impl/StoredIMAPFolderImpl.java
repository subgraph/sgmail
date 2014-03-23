package com.subgraph.sgmail.messages.impl;

import ca.odell.glazedlists.EventList;
import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.accounts.IMAPAccount;
import com.subgraph.sgmail.messages.StoredIMAPFolder;
import com.subgraph.sgmail.messages.StoredIMAPMessage;
import com.subgraph.sgmail.messages.StoredMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StoredIMAPFolderImpl extends StoredFolderImpl implements StoredIMAPFolder {

    private final IMAPAccount imapAccount;

    private long uidValidity;
    private long uidNext;
    private long highestModSeq;

    private transient List<Long> cachedUIDMap;

    public StoredIMAPFolderImpl(IMAPAccount account, String name) {
        super(name);
        this.imapAccount = account;
    }

    @Override
    public IMAPAccount getIMAPAccount() {
        activate(ActivationPurpose.READ);
        return imapAccount;
    }

    @Override
    public List<StoredIMAPMessage> getIMAPMessages() {
        final List<StoredIMAPMessage> list = new ArrayList<>();
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().readLock().lock();
        try {
           for(StoredMessage msg: eventList) {
              list.add(downcastMessage(msg));
           }
            return list;
        } finally {
            eventList.getReadWriteLock().readLock().unlock();
        }
    }

    private static StoredIMAPMessage downcastMessage(StoredMessage message) {
        if(!(message instanceof StoredIMAPMessage)) {
            throw new IllegalStateException("StoredIMAPFolder contains message which is not instance of StoredIMAPMessage "+ message);
        }
        return (StoredIMAPMessage) message;
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
    public List<Long> getUIDMap() {
        synchronized (this) {
            if(cachedUIDMap == null) {
               cachedUIDMap = generateUIDMap();
            }
            return cachedUIDMap;
        }
    }

    private List<Long> generateUIDMap() {
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().readLock().lock();
        try {
            final List<Long> result = new ArrayList<>(eventList.size());
            for(StoredMessage msg: eventList) {
                result.add(downcastMessage(msg).getMessageUID());
            }
            return result;
        } finally {
            eventList.getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public void clearFolder() {
        activate(ActivationPurpose.WRITE);
        uidNext = uidValidity = highestModSeq = 0;
        super.clearFolder();
    }

    @Override
    public StoredIMAPMessage getMessageByMessageNumber(int number) {
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().readLock().lock();
        try {
            if(number < 1 || number > eventList.size()) {
                throw new IllegalArgumentException("Invalid message number "+ number + " folder size: "+ eventList.size());
            }
            return downcastMessage(eventList.get(number - 1));
        } finally {
            eventList.getReadWriteLock().readLock().unlock();
        }
    }

    @Override
    public void addMessage(StoredIMAPMessage message) {
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().writeLock().lock();
        try {
            message.setMessageNumber(eventList.size() + 1);
            super.addMessage(message);
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
            imapAccount.addMessage(message);
        }
    }

    @Override
    public void expungeMessages() {
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().writeLock().lock();
        try {
            final List<StoredMessage> deletedMessages = super.performExpunge();
            cachedUIDMap = null;
            renumberAllMessages(eventList);
            removeMessagesFromDatabaseAndAccount(deletedMessages);
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
        }
    }

    private static void renumberAllMessages(List<StoredMessage> messageList) {
        for(int i =  0; i < messageList.size(); i++) {
            downcastMessage(messageList.get(i)).setMessageNumber(i + 1);
        }
    }

    @Override
    public void expungeMessagesByUID(List<Long> uids) {
        final List<StoredMessage> savedMessages = new ArrayList<>();
        final List<StoredMessage> removedMessages = new ArrayList<>();
        final EventList<StoredMessage> eventList = getMessageEventList();
        eventList.getReadWriteLock().writeLock().lock();
        try {
            sortExpungedMessagesByUID(eventList, new HashSet<>(uids), savedMessages, removedMessages);
            eventList.clear();
            eventList.addAll(savedMessages);
            cachedUIDMap = null;
        } finally {
            eventList.getReadWriteLock().writeLock().unlock();
            removeMessagesFromDatabaseAndAccount(removedMessages);
        }
    }

    private static void sortExpungedMessagesByUID(List<StoredMessage> sourceList, Set<Long> uids, List<StoredMessage> saved, List<StoredMessage> removed) {
        for(StoredMessage msg: sourceList) {
            StoredIMAPMessage imapMessage = downcastMessage(msg);
            if(uids.contains(imapMessage.getMessageUID())) {
                removed.add(msg);
            } else {
                saved.add(msg);
                imapMessage.setMessageNumber(saved.size());
            }
        }
    }

    private void removeMessagesFromDatabaseAndAccount(List<StoredMessage> removedMessages) {
        for(StoredMessage msg: removedMessages) {
            imapAccount.removeMessage(msg);
            model.delete(msg);
        }
        model.commit();
    }
}
