package com.subgraph.sgmail.internal.autoconf;

import com.subgraph.sgmail.autoconf.ServerInformation;

public class ServerInformationImpl implements ServerInformation {

	public static class Builder {
		private Protocol protocol = Protocol.UNKNOWN;
        private String onionHostname;
		private String hostname;
		private int port;
		private SocketType socketType = SocketType.UNKNOWN;
		private AuthenticationType authenticationType = AuthenticationType.UNKNOWN;
		private UsernameType usernameType = UsernameType.UNKNOWN;
		
		ServerInformationImpl build() {
			return new ServerInformationImpl(this);
		}
		
		Builder protocol(String val) { protocol = stringToProtocol(val); return this; }
        Builder protocol(Protocol p) { this.protocol = p; return this; }
        Builder onion(String s) { this.onionHostname = s; return this; }
		Builder hostname(String s) { hostname = s; return this; }
		Builder port(int p) { port = p; return this; }
		Builder socketType(String s) { socketType = stringToSocketType(s); return this; }
        Builder socketType(SocketType st) { socketType = st; return this; }
		Builder authenticationType(String s) { authenticationType = stringToAuthenticationType(s); return this; }
        Builder authenticationType(AuthenticationType at) { authenticationType = at; return this; }
		Builder usernameType(String s) { usernameType = stringToUsernameType(s); return this; }
        Builder usernameType(UsernameType ut) { usernameType = ut; return this; }
	}
	
	
	private static Protocol stringToProtocol(String s) {
		if("imap".equalsIgnoreCase(s)) {
			return Protocol.IMAP;
		} else if("pop3".equalsIgnoreCase(s)) {
			return Protocol.POP3;
		} else if("smtp".equalsIgnoreCase(s)) { 
			return Protocol.SMTP;
		} else {
			return Protocol.UNKNOWN;
		}
	}
	
	private static SocketType stringToSocketType(String s) {
		if("SSL".equalsIgnoreCase(s)) {
			return SocketType.SSL;
		} else if("STARTTLS".equalsIgnoreCase(s)) {
			return SocketType.STARTTLS;
		} else {
			return SocketType.UNKNOWN;
		}
	}
	
	private static AuthenticationType stringToAuthenticationType(String s) {
		if("password-cleartext".equalsIgnoreCase(s)) {
			return AuthenticationType.PASSWORD_CLEAR;
		} else if("password-encrypted".equalsIgnoreCase(s)) {
			return AuthenticationType.PASSWORD_ENCRYPTED;
		} else {
			return AuthenticationType.UNKNOWN;
		}
	}
	
	private static UsernameType stringToUsernameType(String s) {
		if("%EMAILADDRESS%".equalsIgnoreCase(s)) {
			return UsernameType.USERNAME_EMAILADDRESS;
		} else if ("%EMAILLOCALPART%".equalsIgnoreCase(s)) {
			return UsernameType.USERNAME_LOCALPART;
		} else {
			return UsernameType.UNKNOWN;
		}
	}
	
	
	private final Protocol protocol;
	private final String hostname;
    private final String onionHostname;
	private final int port;
	private final SocketType socketType;
	private final AuthenticationType authType;
	private final UsernameType usernameType;

	private ServerInformationImpl(Builder builder) {
		this.protocol = builder.protocol;
		this.hostname = builder.hostname;
        this.onionHostname = builder.onionHostname;
		this.port = builder.port;
		this.socketType = builder.socketType;
		this.authType = builder.authenticationType;
		this.usernameType = builder.usernameType;
	}
	
	public Protocol getProtocol() {
		return protocol;
	}
	
	public String getHostname() {
		return hostname;
	}

    public String getOnionHostname() {
        return onionHostname;
    }
	
	public int getPort() {
		return port;
	}
	
	public SocketType getSocketType() {
		return socketType;
	}
	
	public AuthenticationType getAuthenticationType() {
		return authType;
	}
	
	public UsernameType getUsernameType() {
		return usernameType;
	}

}
