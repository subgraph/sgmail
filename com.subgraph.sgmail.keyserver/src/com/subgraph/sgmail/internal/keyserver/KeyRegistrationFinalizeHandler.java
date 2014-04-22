package com.subgraph.sgmail.identity.server;

import com.subgraph.sgmail.identity.protocol.KeyRegistrationFinalizeRequest;
import com.subgraph.sgmail.identity.protocol.KeyRegistrationFinalizeResponse;
import com.subgraph.sgmail.identity.protocol.Message;

public class KeyRegistrationFinalizeHandler implements MessageHandler {

    private final Server server;

    KeyRegistrationFinalizeHandler(Server server) {
        this.server = server;
    }

    @Override
    public void handleMessage(Message message, ConnectionTask connection) {
        if(message instanceof KeyRegistrationFinalizeRequest) {
            handleKeyRegistrationFinalize((KeyRegistrationFinalizeRequest) message, connection);
        } else {
            throw new IllegalArgumentException("Expecting KeyRegistrationFinalizeRequest and got "+ message.getClass().getName());
        }

    }

    private void handleKeyRegistrationFinalize(KeyRegistrationFinalizeRequest request, ConnectionTask connection) {
        final KeyRegistrationState krs = server.getRegistrationStateByRequestId(request.getRequestId());
        if(krs == null) {
            connection.writeMessage(new KeyRegistrationFinalizeResponse(false, "No registration record found for request id"));
            return;
        }
        if(isHashValid(request.getHashValue(), krs)) {
            server.registerPublicKey(krs);
            connection.writeMessage(new KeyRegistrationFinalizeResponse(true, ""));
        } else {
            connection.writeMessage(new KeyRegistrationFinalizeResponse(false, "Invalid hash value"));
        }

    }

    private boolean isHashValid(byte[] hashValue, KeyRegistrationState krs) {
        return true;
    }
}
