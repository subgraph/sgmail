package com.subgraph.sgmail.model;

import com.db4o.activation.ActivationPurpose;

public class SMTPAccount extends AbstractActivatable {
	
	private final String hostname;
	private final String username;
	private final String password;
	private final int port;
	
	public SMTPAccount(String hostname, int port, String username, String password) {
		this.hostname = hostname;
		this.port = port;
		this.username = username;
		this.password = password;
	}
	
	public String getHostname() {
		activate(ActivationPurpose.READ);
		return hostname;
	}
	
	public int getPort() {
		activate(ActivationPurpose.READ);
		return port;
	}
	
	public String getUsername() {
		activate(ActivationPurpose.READ);
		return username;
	}
	
	public String getPassword() {
		activate(ActivationPurpose.READ);
		return password;
	}
}
