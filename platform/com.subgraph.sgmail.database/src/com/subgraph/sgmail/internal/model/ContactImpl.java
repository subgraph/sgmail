package com.subgraph.sgmail.internal.model;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.database.Contact;

public class ContactImpl implements Contact, Activatable {
  
  private final String emailAddress;
  private String realName;

  private transient Activator activator;
  
  ContactImpl(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  @Override
  public String getEmailAddress() {
    activate(ActivationPurpose.READ);
    return emailAddress;
  }

  @Override
  public String getRealName() {
    activate(ActivationPurpose.READ);
    return realName;
  }

  @Override
  public InternetAddress toInternetAddress() throws AddressException {
		return new InternetAddress(emailAddress);
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
}
