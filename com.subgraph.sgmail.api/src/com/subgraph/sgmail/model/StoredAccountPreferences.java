package com.subgraph.sgmail.model;

import com.subgraph.sgmail.accounts.Account;

public class StoredAccountPreferences extends StoredPreferences {
	
	public static StoredAccountPreferences create(Account account) {
		final StoredAccountPreferences preferences = new StoredAccountPreferences(account);
		Preferences.initializeAccountPreferences(preferences);
		return preferences;
	}

	final Account account;
	
	private StoredAccountPreferences(Account account) {
		this.account = account;
	}
	
	public Account getAccount() {
		return account;
	}
}
