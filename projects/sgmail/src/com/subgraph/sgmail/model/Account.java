package com.subgraph.sgmail.model;

import javax.mail.Store;

public interface Account {
	String getLabel();
	Store getRemoteStore();
}
