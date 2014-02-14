package com.subgraph.sgmail.model;

public class StoredAccountPreferences extends StoredPreferences {
	
	public static StoredAccountPreferences create(Account account, Model model) {
		final StoredAccountPreferences preferences = new StoredAccountPreferences(account, model);
		Preferences.initializeAccountPreferences(preferences);
		return preferences;
	}

	final Account account;
	
	private StoredAccountPreferences(Account account, Model model) {
		this.account = account;
		this.model = model;
	}
	
	public Account getAccount() {
		return account;
	}
}
