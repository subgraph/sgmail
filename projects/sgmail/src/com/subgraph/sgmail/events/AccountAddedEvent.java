package com.subgraph.sgmail.events;

import com.subgraph.sgmail.accounts.Account;

import static com.google.common.base.Preconditions.checkNotNull;

public class AccountAddedEvent {
	
	private final Account account;
	
	public AccountAddedEvent(Account account) {
		this.account = checkNotNull(account);
	}
	
	public Account getAccount() {
		return account;
	}
}
