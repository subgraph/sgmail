package com.subgraph.sgmail.internal.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.collections.ActivatableArrayList;
import com.db4o.ta.Activatable;
import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.database.Contact;
import com.subgraph.sgmail.database.ContactManager;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Storeable;

public class ContactManagerImpl implements ContactManager, Storeable, Activatable {
  private final List<Contact> contactList = new ActivatableArrayList<>();

  private transient Activator activator;
  private transient Database database;

  @Override
  public synchronized List<Contact> getAllContacts() {
    activate(ActivationPurpose.READ);
    return ImmutableList.copyOf(contactList);
  }

  @Override
  public synchronized Contact getContactByEmail(String emailAddress) {
    activate(ActivationPurpose.READ);
    for (Contact c : contactList) {
      if (c.getEmailAddress().equalsIgnoreCase(emailAddress)) {
        return c;
      }
    }
    final Contact c = new ContactImpl(emailAddress);
    database.store(c);
    contactList.add(c);
    database.commit();
    return c;
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
