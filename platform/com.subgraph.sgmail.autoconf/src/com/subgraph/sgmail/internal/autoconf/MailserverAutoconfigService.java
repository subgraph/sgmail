package com.subgraph.sgmail.internal.autoconf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.subgraph.sgmail.autoconf.AutoconfigResult;
import com.subgraph.sgmail.autoconf.MailserverAutoconfig;
import com.subgraph.sgmail.autoconf.ServerInformation;

public class MailserverAutoconfigService implements MailserverAutoconfig {

    private static class DomainEntry {
        private final ServerInformationImpl incomingServer;
        private final ServerInformationImpl outgoingServer;
        DomainEntry(ServerInformationImpl incomingServer, ServerInformationImpl outgoingServer) {
            this.incomingServer = incomingServer;
            this.outgoingServer = outgoingServer;
        }
        ServerInformationImpl getIncomingServer() {
            return incomingServer;
        }

        ServerInformationImpl getOutgoingServer() {
            return outgoingServer;
        }
    }
    private final static Map<String, DomainEntry> domainEntries = new HashMap<>();

    static {
        addDomainEntry("gmail.com", "imap.googlemail.com", "smtp.googlemail.com");
        addDomainEntry("subgraph.com", "imap.googlemail.com", "smtp.googlemail.com");
        addDomainEntry("riseup.net", "imap.riseup.net", "xyvp43vrggckj427.onion", "smtp.riseup.net", "xyvp43vrggckj427.onion");
    }

    private static void addDomainEntry(String domain, String imapHostname, String imapOnionname, String smtpHostname, String smtpOnionname) {
        final ServerInformationImpl incomingServer = createImapServer(imapHostname, imapOnionname);
        final ServerInformationImpl outgoingServer = createSMTPServer(smtpHostname, smtpOnionname);
        addDomainEntry(domain, incomingServer, outgoingServer);
    }

    private static void addDomainEntry(String domain, String imapHostname, String smtpHostname) {
        final ServerInformationImpl incomingServer = createImapServer(imapHostname);
        final ServerInformationImpl outgoingServer = createSMTPServer(smtpHostname);
        addDomainEntry(domain, incomingServer, outgoingServer);
    }

    private static void addDomainEntry(String domain, ServerInformationImpl incomingServer, ServerInformationImpl outgoingServer) {
        final DomainEntry entry = new DomainEntry(incomingServer, outgoingServer);
        domainEntries.put(domain, entry);
    }

    private static ServerInformationImpl createImapServer(String hostname, String onion) {
        return createIMAPBuilder().hostname(hostname).onion(onion).build();
    }
    private static ServerInformationImpl createImapServer(String hostname) {
        return createIMAPBuilder().hostname(hostname).build();
    }

    private static ServerInformationImpl createSMTPServer(String hostname, String onion) {
        return createSMTPBuilder().hostname(hostname).onion(onion).build();
    }

    private static ServerInformationImpl createSMTPServer(String hostname) {
        return createSMTPBuilder().hostname(hostname).build();
    }

    private static ServerInformationImpl.Builder createIMAPBuilder() {
        return new ServerInformationImpl.Builder()
                .protocol(ServerInformationImpl.Protocol.IMAP)
                .socketType(ServerInformationImpl.SocketType.SSL)
                .port(993)
                .authenticationType(ServerInformationImpl.AuthenticationType.PASSWORD_CLEAR)
                .usernameType(ServerInformationImpl.UsernameType.USERNAME_EMAILADDRESS);
    }

    private static ServerInformationImpl.Builder createSMTPBuilder() {
        return new ServerInformationImpl.Builder()
                .protocol(ServerInformationImpl.Protocol.SMTP)
                .socketType(ServerInformationImpl.SocketType.SSL)
                .port(465)
                .authenticationType(ServerInformationImpl.AuthenticationType.PASSWORD_CLEAR)
                .usernameType(ServerInformationImpl.UsernameType.USERNAME_EMAILADDRESS);
    }

	@Override
	public AutoconfigResult resolveDomain(String domainName) {
		final List<ServerInformation> incomingServers = new ArrayList<>();
		final List<ServerInformation> outgoingServers = new ArrayList<>();
		final String dn = domainName.toLowerCase();
		
		if(domainEntries.containsKey(dn)) {
			final DomainEntry entry = domainEntries.get(dn);
			incomingServers.add(entry.getIncomingServer());
			outgoingServers.add(entry.getOutgoingServer());
			return new AutoconfigResultImpl(incomingServers, outgoingServers);
		}
		final MozillaAutoconfiguration mozillaAutoconf = new MozillaAutoconfiguration(dn);
		if(mozillaAutoconf.performLookup()) {
			incomingServers.addAll(mozillaAutoconf.getIncomingServers());
			outgoingServers.addAll(mozillaAutoconf.getOutgoingServers());
			return new AutoconfigResultImpl(incomingServers, outgoingServers);
		}
		return null;
	}
}
