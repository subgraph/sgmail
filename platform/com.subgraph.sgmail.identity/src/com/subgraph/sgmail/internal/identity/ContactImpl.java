package com.subgraph.sgmail.internal.identity;

import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.identity.Contact;
import com.subgraph.sgmail.identity.PublicIdentity;

public class ContactImpl implements Contact {
	
	private final String emailAddress;

	ContactImpl(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	@Override
	public String getEmailAddress() {
		return emailAddress;
	}

	@Override
	public String getRealName() {
		return null;
	}

	@Override
	public PublicIdentity getPublicIdentity() {
		return null;
	}

	@Override
	public InternetAddress toInternetAddress() throws AddressException {
		return new InternetAddress(emailAddress);
	}

	@Override
	public List<PublicIdentity> getLocalPublicKeys() {
		return ImmutableList.of();
	}
}
