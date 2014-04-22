package com.subgraph.sgmail.internal.imap;

import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.accounts.ServerDetails;
import com.subgraph.sgmail.imap.IMAPAccount;
import com.subgraph.sgmail.imap.IMAPAccountList;
import com.subgraph.sgmail.imap.IMAPFactory;

public class IMAPFactoryService implements IMAPFactory {

	@Override
	public IMAPAccountList createIMAPAccountList() {
		return new IMAPAccountListImpl();
	}

	@Override
	public IMAPAccount createIMAPAccount(MailAccount mailAccount,
			ServerDetails imapServerDetails) {
		return new IMAPAccountImpl(mailAccount, imapServerDetails);
	}

}
