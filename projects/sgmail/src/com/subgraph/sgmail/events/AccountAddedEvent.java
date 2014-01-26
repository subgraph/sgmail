package com.subgraph.sgmail.events;

import static com.google.common.base.Preconditions.checkNotNull;

import com.subgraph.sgmail.model.Account;

public class AccountAddedEvent {
	
	private final Account account;
	
	public AccountAddedEvent(Account account) {
		this.account = checkNotNull(account);
	}
	
	public Account getAccount() {
		return account;
	}
}
