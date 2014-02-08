package com.subgraph.sgmail.model;

import javax.mail.Store;

public interface Account {
	String getLabel();
	Store getRemoteStore();
	void setIdentity(Identity identity);
	Identity getIdentity();
}
