package com.subgraph.sgmail.servers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailserverAutoconfiguration {

    private static class DomainEntry {
        private final ServerInformation incomingServer;
        private final ServerInformation outgoingServer;
        DomainEntry(ServerInformation incomingServer, ServerInformation outgoingServer) {
            this.incomingServer = incomingServer;
            this.outgoingServer = outgoingServer;
        }
        ServerInformation getIncomingServer() {
            return incomingServer;
        }

        ServerInformation getOutgoingServer() {
            return outgoingServer;
        }
    }
    private final static Map<String, DomainEntry> domainEntries = new HashMap<>();

    private final String domain;
    private final List<ServerInformation> incomingServers = new ArrayList<>();
    private final List<ServerInformation> outgoingServers = new ArrayList<>();

    static {
        addDomainEntry("gmail.com", "imap.googlemail.com", "smtp.googlemail.com");
        addDomainEntry("subgraph.com", "imap.googlemail.com", "smtp.googlemail.com");
    }

    private static void addDomainEntry(String domain, String imapHostname, String smtpHostname) {
        final ServerInformation incomingServer = createImapServer(imapHostname);
        final ServerInformation outgoingServer = createSMTPServer(smtpHostname);
        addDomainEntry(domain, incomingServer, outgoingServer);
    }

    private static void addDomainEntry(String domain, ServerInformation incomingServer, ServerInformation outgoingServer) {
        final DomainEntry entry = new DomainEntry(incomingServer, outgoingServer);
        domainEntries.put(domain, entry);
    }

    private static ServerInformation createImapServer(String hostname) {
        return createIMAPBuilder().hostname(hostname).build();
    }

    private static ServerInformation createSMTPServer(String hostname) {
        return createSMTPBuilder().hostname(hostname).build();
    }

    private static ServerInformation.Builder createIMAPBuilder() {
        return new ServerInformation.Builder()
                .protocol(ServerInformation.Protocol.IMAP)
                .socketType(ServerInformation.SocketType.SSL)
                .port(993)
                .authenticationType(ServerInformation.AuthenticationType.PASSWORD_CLEAR)
                .usernameType(ServerInformation.UsernameType.USERNAME_EMAILADDRESS);
    }

    private static ServerInformation.Builder createSMTPBuilder() {
        return new ServerInformation.Builder()
                .protocol(ServerInformation.Protocol.SMTP)
                .socketType(ServerInformation.SocketType.SSL)
                .port(465)
                .authenticationType(ServerInformation.AuthenticationType.PASSWORD_CLEAR)
                .usernameType(ServerInformation.UsernameType.USERNAME_EMAILADDRESS);
    }

    public MailserverAutoconfiguration(String domain) {
        this.domain = domain.toLowerCase();
    }

    public boolean performLookup() {
        incomingServers.clear();
        outgoingServers.clear();
        if(domainEntries.containsKey(domain)) {
            final DomainEntry entry = domainEntries.get(domain);
            incomingServers.add(entry.getIncomingServer());
            outgoingServers.add(entry.getOutgoingServer());
            return true;
        }
        final MozillaAutoconfiguration mozillaAutoconf = new MozillaAutoconfiguration(domain);
        if(mozillaAutoconf.performLookup()) {
            incomingServers.addAll(mozillaAutoconf.getIncomingServers());
            outgoingServers.addAll(mozillaAutoconf.getOutgoingServers());
            return true;
        }

        return false;
    }

    public List<ServerInformation> getIncomingServers() {
        return incomingServers;
    }

    public List<ServerInformation> getOutgoingServers() {
        return outgoingServers;
    }
}
