package com.subgraph.sgmail.ui.dialogs;

import java.lang.reflect.InvocationTargetException;

import com.subgraph.sgmail.servers.MailserverAutoconfiguration;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import com.subgraph.sgmail.servers.ServerInformation;
import com.subgraph.sgmail.servers.ServerInformation.Protocol;
import com.subgraph.sgmail.servers.MozillaAutoconfiguration;

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
        MailserverAutoconfiguration autoconf = new MailserverAutoconfiguration(domain);
		monitor.beginTask("Looking up server information", 1);
		if(autoconf.performLookup()) {
			final ServerInformation incoming = getIMAPServer(autoconf);
			final ServerInformation outgoing = getSMTPServer(autoconf);
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
	private ServerInformation getIMAPServer(MailserverAutoconfiguration autoconf) {
		for(ServerInformation info: autoconf.getIncomingServers()) {
			if(info.getProtocol() == Protocol.IMAP) {
				return info;
			}
		}
		return null;
	}
	
	private ServerInformation getSMTPServer(MailserverAutoconfiguration autoconf) {
		for(ServerInformation info: autoconf.getOutgoingServers()) {
			return info;
		}
		return null;
	}




		


}
