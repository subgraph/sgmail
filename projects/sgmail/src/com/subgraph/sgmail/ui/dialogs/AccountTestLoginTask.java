package com.subgraph.sgmail.ui.dialogs;

import com.subgraph.sgmail.servers.ServerInformation;

public class AccountTestLoginTask implements Runnable {

	private final ServerInformation server;
	private final String login;
	private final String password;
	
	AccountTestLoginTask(ServerInformation server, String login, String password) {
		this.server = server;
		this.login = login;
		this.password = password;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

}
