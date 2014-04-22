package com.subgraph.sgmail.ui.dialogs;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.subgraph.sgmail.autoconf.AutoconfigResult;
import com.subgraph.sgmail.autoconf.MailserverAutoconfig;
import com.subgraph.sgmail.autoconf.ServerInformation;
import com.subgraph.sgmail.ui.Activator;

public class AccountLookupTask implements IRunnableWithProgress {

	private final AccountDetailsPage page;
	private final NewAccountDialog dialog;
	private final String domain;
	private boolean lookupSucceeded;
	
	public AccountLookupTask(AccountDetailsPage page, String domain) {
		this.page = page; this.dialog = null;
		this.domain = domain;
	}
	public AccountLookupTask(NewAccountDialog dialog, String domain) {
		this.dialog = dialog; page = null;
		this.domain = domain;
	}
	
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException {
		final MailserverAutoconfig autoconf = Activator.getInstance().getMailserverAutoconfig();
		monitor.beginTask("Lookup up server information", 1);
		final AutoconfigResult result = autoconf.resolveDomain(domain);
		if(result != null) {
			final ServerInformation incoming = getIMAPServer(result);
			final ServerInformation outgoing = getSMTPServer(result);
			if(dialog != null) dialog.setServerInfo(incoming, outgoing);
			if(page != null) page.setServerInfo(incoming, outgoing);
			lookupSucceeded = true;
		} else {
			// XXX notify user that autoconf did not complete successfully
		}
		monitor.done();
	}
	
	public boolean getLookupSucceeded() {
		return lookupSucceeded;
	}
	
	private ServerInformation getIMAPServer(AutoconfigResult result) {
		for(ServerInformation info: result.getIncomingServers()) {
			if(info.getProtocol() == ServerInformation.Protocol.IMAP) {
				return info;
			}
		}
		return null;
	}
	
	private ServerInformation getSMTPServer(AutoconfigResult result) {
		for(ServerInformation info: result.getOutgoingServers()) {
			return info;
		}
		return null;
	}
}
