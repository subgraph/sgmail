package com.subgraph.sgmail.accounts;

import ca.odell.glazedlists.EventList;

public interface AccountList {
	EventList<Account> getAccounts();
	void addAccount(Account account);
	void removeAccount(Account account);
}
