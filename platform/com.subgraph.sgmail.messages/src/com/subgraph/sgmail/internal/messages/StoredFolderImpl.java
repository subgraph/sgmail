package com.subgraph.sgmail.internal.messages;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Storeable;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.messages.StoredMessage;

public class StoredFolderImpl implements StoredFolder, Activatable, Storeable {

  private final ArrayList<StoredMessage> messages = new ArrayList<>();
  private final Account account;
  private String name;

  private transient EventList<StoredMessage> messageEventList;
  private transient EventList<StoredMessage> readOnlyEventList;
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
  public EventList<StoredMessage> getMessageEventList() {
    synchronized (messages) {
      if (readOnlyEventList == null) {
        readOnlyEventList = GlazedLists.readOnlyList(getWritableMessageEventList());
      }
    }
    return readOnlyEventList;
  }

  @SuppressWarnings("deprecation")
  private EventList<StoredMessage> getWritableMessageEventList() {
    activate(ActivationPurpose.READ);
    synchronized (messages) {
      if (messageEventList == null) {
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
    final EventList<StoredMessage> eventList = getWritableMessageEventList();
    eventList.getReadWriteLock().writeLock().lock();
    try {
      eventList.add(message);
      message.incrementReferenceCount();
    } finally {
      database.store(messages);
      eventList.getReadWriteLock().writeLock().unlock();
      database.commit();
    }
  }

  @Override
  public void expungeMessages() {
    final List<StoredMessage> deletedMessages = performExpunge();
    for (StoredMessage msg : deletedMessages) {
      database.delete(msg);
    }
    database.commit();
  }

  protected List<StoredMessage> performExpunge() {
    final EventList<StoredMessage> eventList = getWritableMessageEventList();
    eventList.getReadWriteLock().writeLock().lock();
    try {
      if (!hasDeletedMessages(eventList)) {
        return Collections.emptyList();
      }
      final List<StoredMessage> savedMessages = new ArrayList<>();
      final List<StoredMessage> deletedMessages = new ArrayList<>();
      sortDeletedMessages(eventList, savedMessages, deletedMessages);
      eventList.clear();
      eventList.addAll(savedMessages);
      return deletedMessages;
    } finally {
      database.store(messages);
      eventList.getReadWriteLock().writeLock().unlock();
    }
  }

  private static boolean hasDeletedMessages(List<StoredMessage> sourceList) {
    for (StoredMessage msg : sourceList) {
      if (msg.isFlagSet(StoredMessage.FLAG_DELETED)) {
        return true;
      }
    }
    return false;
  }

  private static void sortDeletedMessages(List<StoredMessage> sourceList, List<StoredMessage> savedMessages,
      List<StoredMessage> deletedMessages) {
    for (StoredMessage msg : sourceList) {
      if (msg.isFlagSet(StoredMessage.FLAG_DELETED)) {
        deletedMessages.add(msg);
      } else {
        savedMessages.add(msg);
      }
    }
  }

  @Override
  public void clearFolder() {
    final EventList<StoredMessage> eventList = getWritableMessageEventList();
    eventList.getReadWriteLock().writeLock().lock();
    try {
      for (StoredMessage msg : eventList) {
        msg.decrementReferenceCount();
      }
      eventList.clear();
    } finally {
      database.store(messages);
      eventList.getReadWriteLock().writeLock().unlock();
    }
  }

  @Override
  public void activate(ActivationPurpose activationPurpose) {
    if (activator != null) {
      activator.activate(activationPurpose);
    }
  }

  @Override
  public void bind(Activator activator) {
    if (this.activator == activator) {
      return;
    }
    if (activator != null && this.activator != null) {
      throw new IllegalStateException("Object can only be bound one to an activator");
    }
    this.activator = activator;
  }

  @Override
  public void setDatabase(Database database) {
    this.database = checkNotNull(database);
  }

}
