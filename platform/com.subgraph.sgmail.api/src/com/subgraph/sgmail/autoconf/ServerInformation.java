package com.subgraph.sgmail.autoconf;

public interface ServerInformation {
	enum Protocol { IMAP, POP3, SMTP, UNKNOWN };
	enum SocketType { PLAIN, SSL, STARTTLS, UNKNOWN };
	enum AuthenticationType { PASSWORD_CLEAR, PASSWORD_ENCRYPTED, UNKNOWN };
	enum UsernameType { USERNAME_LOCALPART, USERNAME_EMAILADDRESS, UNKNOWN };
	
	Protocol getProtocol();
	String getHostname();
	String getOnionHostname();
	int getPort();
	SocketType getSocketType();
	AuthenticationType getAuthenticationType();
	UsernameType getUsernameType();
}
