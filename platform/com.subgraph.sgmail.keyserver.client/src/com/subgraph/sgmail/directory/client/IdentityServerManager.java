package com.subgraph.sgmail.directory.client;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.database.Preferences;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IdentityServerManager {

    private final Model model;
    private final ListeningExecutorService executor;
    private IdentityServerConnection connection;

    public IdentityServerManager(Model model, ListeningExecutorService executor) {
    	this.model = model;
    	this.executor = executor;
    }

    private Map<String, ListenableFuture<KeyLookupResult>> pendingEmailLookups = new HashMap<>();

    public ListenableFuture<KeyLookupResult> lookupKeyByEmailAddress(String emailAddress) {
        synchronized (pendingEmailLookups) {
            if(!pendingEmailLookups.containsKey(emailAddress)) {
                final KeyLookupTask task = new KeyLookupTask(this, emailAddress);
                pendingEmailLookups.put(emailAddress, executor.submit(task));
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
    	return model.getRootPreferences().getPreference(Preferences.IDENTITY_SERVER_ADDRESS);
    }
}
