package com.subgraph.sgmail.ui.dialogs;

import com.subgraph.sgmail.autoconf.ServerInformation;
import com.sun.mail.util.MailConnectException;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.core.runtime.IProgressMonitor;

import javax.mail.*;

import java.util.Properties;
import java.util.logging.Logger;

public class AccountTestLoginTask implements IRunnableWithProgress {
	private final static Logger logger = Logger.getLogger(AccountTestLoginTask.class.getName());
	
	private final Properties sessionProperties;
	private final ServerInformation server;
	private final String login;
	private final String password;
	private final boolean useTor;
    private final boolean debug;

	private boolean isSuccess;
	private String errorMessage = "";
	
	AccountTestLoginTask(ServerInformation server, String login, String password, boolean useTor, boolean debug) {
		this.server = server;
		this.login = login;
		this.password = password;
        this.useTor = useTor;
        this.debug = debug;
		this.sessionProperties = new Properties();
	}

	public boolean isSuccess() {
		return isSuccess;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	@Override
	public void run(IProgressMonitor monitor) {
		monitor.beginTask("Test login credentials", IProgressMonitor.UNKNOWN);
		
		final String protocol = getProtocolName();
		setupSessionProperties(protocol);
		Session session = Session.getInstance(sessionProperties);
        if(debug) {
            session.setDebug(true);
        }
		
		Store store;
		try {
			store = session.getStore(protocol);
			store.connect(login, password);
			store.close();
			isSuccess = true;
		} catch (AuthenticationFailedException e) {
			isSuccess = false;
			errorMessage = "Login failed";
		} catch (NoSuchProviderException e) {
			isSuccess = false;
			errorMessage = e.getMessage();
			logger.warning("Could not test login credentials: "+ e.getMessage());
        } catch (MailConnectException e) {
            isSuccess = false;
            errorMessage = "Could not connect to "+ e.getHost();
		} catch (MessagingException e) {
			isSuccess = false;
			errorMessage = e.getMessage();
			logger.warning("Error testing login credentials: "+ e.getMessage());
		}
	}
	
	private void setupSessionProperties(String protocol) {
		
		set("mail.store.protocol", protocol);
        set("mail."+ protocol + ".host", getConnectHostname());
		set("mail."+ protocol +".port", Integer.toString(server.getPort()));
		
		if(server.getSocketType() == ServerInformation.SocketType.STARTTLS) {
			set("mail."+ protocol +".starttls.enable", "true");
			set("mail."+ protocol +".starttls.required", "true");
		}
	}

    private String getConnectHostname() {
        if(useTor && server.getOnionHostname() != null) {
            logger.info("Using onion address for testing login credentials: "+ server.getOnionHostname());
            return server.getOnionHostname();
        } else {
            return server.getHostname();
        }
    }

	private void set(String name, String value) {
		sessionProperties.setProperty(name, value);
	}
	
	
	private boolean isSecureSocket() {
		return server.getSocketType() == ServerInformation.SocketType.SSL;
	}
	
	private String getProtocolName() {
		final boolean ss = isSecureSocket();
		switch(server.getProtocol()) {
		case IMAP:
			return (ss) ? ("imaps") : ("imap");

		case POP3:
			return (ss) ? ("pop3s") : ("pop3");
			
		case SMTP:
		case UNKNOWN:
		default:
			throw new IllegalArgumentException("Incoming server protocol has invalid value "+ server.getProtocol());
		}
	}


}
