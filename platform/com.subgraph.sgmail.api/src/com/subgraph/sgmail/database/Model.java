package com.subgraph.sgmail.database;

import com.subgraph.sgmail.accounts.AccountList;

public interface Model {
	int getModelVersion();
	AccountList getAccountList();
	StoredUserInterfaceState getStoredUserInterfaceState();
	Preferences getRootPreferences();
	Preferences createNewAccountPreferences();
	int getNextUniqueId();
	Database getDatabase();
}
