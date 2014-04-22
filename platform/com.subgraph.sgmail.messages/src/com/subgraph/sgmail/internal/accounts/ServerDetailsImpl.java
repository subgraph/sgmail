package com.subgraph.sgmail.internal.accounts;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.accounts.AuthenticationCredentials;
import com.subgraph.sgmail.accounts.PasswordAuthenticationCredentials;
import com.subgraph.sgmail.accounts.ServerDetails;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import java.util.logging.Logger;

public class ServerDetailsImpl implements ServerDetails, Activatable {
    private final static Logger logger = Logger.getLogger(ServerDetailsImpl.class.getName());

    private final String protocol;
    private final String hostname;
    private final String onionHostname;
    private final int port;
    private final AuthenticationCredentials credentials;

	private transient Activator activator;

    public ServerDetailsImpl(String protocol, String hostname, String onionHostname, int port, AuthenticationCredentials credentials) {
        this.protocol = protocol;
        this.hostname = hostname;
        this.onionHostname = onionHostname;
        this.port = port;
        this.credentials = credentials;
    }

    @Override
    public String getProtocol() {
        activate(ActivationPurpose.READ);
        return protocol;
    }

    @Override
    public String getHostname() {
        activate(ActivationPurpose.READ);
        return hostname;
    }

    @Override
    public String getOnionHostname() {
        activate(ActivationPurpose.READ);
        return onionHostname;
    }

    @Override
    public int getPort() {
        activate(ActivationPurpose.READ);
        return port;
    }

    @Override
    public AuthenticationCredentials getCredentials() {
        activate(ActivationPurpose.READ);
        return credentials;
    }

    @Override
    public boolean isPasswordAuthentication() {
        activate(ActivationPurpose.READ);
        return credentials instanceof PasswordAuthenticationCredentials;
    }

    @Override
    public String getLogin() {
        return getPasswordAuthenticationCredentials().getLogin();
    }

    @Override
    public String getPassword() {
        return getPasswordAuthenticationCredentials().getPassword();
    }

    public Store createRemoteStore(Session session, boolean preferOnionAddress) {
        activate(ActivationPurpose.READ);
        final URLName urlname = getURLName(preferOnionAddress);
        try {
            return session.getStore(urlname);
        } catch (NoSuchProviderException e) {
            logger.warning("Could not create store for "+ urlname + " : "+ e);
            return null;
        }
    }

    private URLName getURLName(boolean preferOnionAddress) {
        final String login = getPasswordAuthenticationCredentials().getLogin();
        final String password = getPasswordAuthenticationCredentials().getPassword();
        final String connectHostname = getConnectHostname(preferOnionAddress);
        //final int portValue = (port == getDefaultPort()) ? -1 : port;
        return new URLName(getProtocol(), connectHostname, port, null, login, password);
    }

    
    private String getConnectHostname(boolean preferOnionAddress) {
        if(onionHostname != null && preferOnionAddress) {
            return onionHostname;
        } else {
            return hostname;
        }
    }
    private PasswordAuthenticationCredentials getPasswordAuthenticationCredentials() {
        if(!(credentials instanceof PasswordAuthenticationCredentials)) {
            throw new IllegalArgumentException("credentials are not password authentication credentials: "+ credentials);
        }
        return (PasswordAuthenticationCredentials) credentials;
    }
    
	@Override
	public void activate(ActivationPurpose activationPurpose) {
		if(activator != null) {
			activator.activate(activationPurpose);
		}
	}

	@Override
	public void bind(Activator activator) {
		if(this.activator == activator) {
			return;
		}
		if(activator != null  && this.activator != null) {
			throw new IllegalStateException("Object can only be bound one to an activator");
		}
		this.activator = activator;
	}
}
