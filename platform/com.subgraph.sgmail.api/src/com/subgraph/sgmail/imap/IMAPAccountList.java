package com.subgraph.sgmail.imap;

import java.util.List;

public interface IMAPAccountList {
	 List<IMAPAccount> getAccounts();

	void addAccount(IMAPAccount account);
}
