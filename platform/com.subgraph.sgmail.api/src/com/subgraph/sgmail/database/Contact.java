package com.subgraph.sgmail.database;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public interface Contact {
  String getEmailAddress();
  String getRealName();
  InternetAddress toInternetAddress() throws AddressException;
}
