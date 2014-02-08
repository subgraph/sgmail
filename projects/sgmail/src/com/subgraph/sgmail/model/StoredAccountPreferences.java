package com.subgraph.sgmail.model;

public class StoredAccountPreferences extends StoredPreferences {
	
	final Account account;
	
	StoredAccountPreferences(Account account, Model model) {
		this.account = account;
		this.model = model;
	}
	
	Account getAccount() {
		return account;
	}
}
