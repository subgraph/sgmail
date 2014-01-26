package com.subgraph.sgmail.ui.dialogs;

import com.subgraph.sgmail.servers.ServerInformation;
import com.subgraph.sgmail.servers.ServerInformation.Protocol;
import com.subgraph.sgmail.servers.MozillaAutoconfiguration;

public class AccountLookupTask implements Runnable{

	private final NewAccountDialog dialog;
	private final String domain;
	
	public AccountLookupTask(NewAccountDialog dialog, String domain) {
		this.dialog = dialog;
		this.domain = domain;
	}
	
	@Override
	public void run() {
		MozillaAutoconfiguration autoconf = new MozillaAutoconfiguration(domain);
		if(autoconf.performLookup()) {
			final ServerInformation incoming = getIMAPServer(autoconf);
			final ServerInformation outgoing = getSMTPServer(autoconf);
			dialog.setServerInfo(incoming, outgoing);
		} else {
			// XXX notify user that autoconf did not complete successfully
		}
	}
	
	private ServerInformation getIMAPServer(MozillaAutoconfiguration autoconf) {
		for(ServerInformation info: autoconf.getIncomingServers()) {
			if(info.getProtocol() == Protocol.IMAP) {
				return info;
			}
		}
		return null;
	}
	
	private ServerInformation getSMTPServer(MozillaAutoconfiguration autoconf) {
		for(ServerInformation info: autoconf.getOutgoingServers()) {
			return info;
		}
		return null;
	}

}
