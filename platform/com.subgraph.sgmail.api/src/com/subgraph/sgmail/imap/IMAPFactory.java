package com.subgraph.sgmail.imap;

import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.accounts.ServerDetails;

public interface IMAPFactory {
	IMAPAccountList createIMAPAccountList();
	IMAPAccount createIMAPAccount(MailAccount mailAccount, ServerDetails imapServerDetails);

}
