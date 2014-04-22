package com.subgraph.sgmail.internal.accounts;

import com.subgraph.sgmail.accounts.AccountFactory;
import com.subgraph.sgmail.accounts.AuthenticationCredentials;
import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.accounts.ServerDetails;
import com.subgraph.sgmail.database.Model;

public class AccountFactoryService implements AccountFactory {

	private Model model;
	
	void setModel(Model model) {
		this.model = model;
	}
	
	@Override
	public ServerDetails createServerDetails(String protocol, String hostname,
			String onionHostname, int port, String login, String password) {
		final AuthenticationCredentials credentials = new PasswordAuthenticationCredentialsImpl(login, password);
		return new ServerDetailsImpl(protocol, hostname, onionHostname, port, credentials);
	}

	@Override
	public MailAccount createMailAccount(String label, String emailAddress,
			String realName, ServerDetails smtpServer) {
		return new BasicMailAccount(label, emailAddress, realName, smtpServer, model.createNewAccountPreferences());
	}

}
