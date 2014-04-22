package com.subgraph.sgmail.identity;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

public interface Contact {
	String getEmailAddress();
	String getRealName();
	PublicIdentity getPublicIdentity();
	InternetAddress toInternetAddress() throws AddressException;
	List<PublicIdentity> getLocalPublicKeys();

}
