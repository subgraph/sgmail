package com.subgraph.sgmail.internal.directory;

import java.util.logging.Logger;

import com.subgraph.sgmail.directory.protocol.KeyLookupRequest;
import com.subgraph.sgmail.directory.protocol.KeyLookupResponse;
import com.subgraph.sgmail.directory.protocol.Message;
import com.subgraph.sgmail.directory.protocol.PublicKeyData;

public class KeyLookupHandler implements MessageHandler {
    private final Logger logger = Logger.getLogger(KeyLookupHandler.class.getName());
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
        logger.info("KeyLookupRequest received for address "+ email);
        if(email != null) {
            final IdentityRecord record = server.lookupRecordByEmail(email);
            if(record != null) {
                logger.info("Record found for address "+ email);
                response.addPublicKey(new PublicKeyData(record.getKeyData()));
            }
        }

        connection.writeMessage(response);
    }
}
