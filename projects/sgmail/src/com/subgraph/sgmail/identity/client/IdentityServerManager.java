package com.subgraph.sgmail.identity.client;

import com.google.common.util.concurrent.ListenableFuture;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.Preferences;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IdentityServerManager {

    private final Model model;
    private IdentityServerConnection connection;

    public IdentityServerManager(Model model) {
        this.model = model;
    }

    private Map<String, ListenableFuture<KeyLookupResult>> pendingEmailLookups = new HashMap<>();

    public ListenableFuture<KeyLookupResult> lookupKeyByEmailAddress(String emailAddress) {
        synchronized (pendingEmailLookups) {
            if(!pendingEmailLookups.containsKey(emailAddress)) {
                final KeyLookupTask task = new KeyLookupTask(this, emailAddress);
                pendingEmailLookups.put(emailAddress, model.submitTask(task));
            }
            return pendingEmailLookups.get(emailAddress);
        }
    }

    synchronized IdentityServerConnection getIdentityServerConnection() throws IOException {
        if(connection == null || !connection.isConnected()) {
            connection = new IdentityServerConnection(getServerAddress());
            connection.connect();
        }
        return connection;
    }

    private String getServerAddress() {
        return model.getRootStoredPreferences().getPreference(Preferences.IDENTITY_SERVER_ADDRESS);
    }
}
