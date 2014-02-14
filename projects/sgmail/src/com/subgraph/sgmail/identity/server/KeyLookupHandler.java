package com.subgraph.sgmail.identity.server;

import com.subgraph.sgmail.identity.protocol.KeyLookupRequest;
import com.subgraph.sgmail.identity.protocol.KeyLookupResponse;
import com.subgraph.sgmail.identity.protocol.Message;
import com.subgraph.sgmail.identity.protocol.PublicKeyData;

public class KeyLookupHandler implements MessageHandler {
    private final Server server;

    KeyLookupHandler(Server server) {
        this.server = server;
    }

    @Override
    public void handleMessage(Message message, ConnectionTask connection) {
        if(message instanceof  KeyLookupRequest) {
            handleKeyLookupRequest((KeyLookupRequest) message, connection);
        } else {
            throw new IllegalArgumentException("KeyLookupHandler expecting KeyLookupRequest, got "+ message.getClass().getName());
        }
    }

    private void handleKeyLookupRequest(KeyLookupRequest request, ConnectionTask connection) {
        final KeyLookupResponse response = new KeyLookupResponse();
        String email = request.getEmailAddress();
        if(email != null) {
            final PublicKeyRecord pkr = server.lookupRecordByEmail(email);
            if(pkr != null) {
                response.addPublicKey(new PublicKeyData(pkr.getKeyData()));
            }
        }
        connection.writeMessage(response);
    }
}
