package com.subgraph.sgmail.accounts;

public interface AccountFactory {
	ServerDetails createServerDetails(String protocol, String hostname, String onionHostname, int port, String login, String password);
	MailAccount createMailAccount(String label, String emailAddress, String realName, ServerDetails smtpServer);
}
