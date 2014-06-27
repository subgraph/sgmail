package com.subgraph.sgmail.database;

import java.util.List;

public interface ContactManager {
  List<Contact> getAllContacts();
  Contact getContactByEmail(String emailAddress);
}
