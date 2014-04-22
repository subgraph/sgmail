package com.subgraph.sgmail.identity.server;

import com.subgraph.sgmail.identity.OpenPGPKeyUtils;
import com.subgraph.sgmail.identity.protocol.KeyRegistrationRequest;
import com.subgraph.sgmail.identity.protocol.KeyRegistrationResponse;
import com.subgraph.sgmail.identity.protocol.Message;

import java.io.IOException;
import java.util.List;

public class KeyRegistrationHandler implements MessageHandler {

    private final Server server;

    public KeyRegistrationHandler(Server server) {
        this.server = server;
    }

    @Override
    public void handleMessage(Message message, ConnectionTask connection) {
        if(message instanceof KeyRegistrationRequest) {
            try {
                handleKeyRegistrationRequest((KeyRegistrationRequest) message, connection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new IllegalArgumentException("Expecting a KeyRegistrationRequest and got "+ message.getClass().getName());
        }
    }

    private void handleKeyRegistrationRequest(KeyRegistrationRequest request, ConnectionTask connection) throws IOException {
           final String email = request.getEmailAddress();
        if(!isValidEmailAddress(email)) {
            connection.writeMessage(KeyRegistrationResponse.createErrorResponse("Email address is not valid"));
            return;
        }

        final byte[] keyData = request.getKeyData();

        final OpenPGPKeyUtils keyUtils = OpenPGPKeyUtils.createFromPublicKeyBytes(keyData);
        //final PublicKeyDecoder decoder = PublicKeyDecoder.createFromBytes(keyData);
        if(!emailMatchesUserIDList(email, keyUtils.getUserIDs())) {
            sendError(connection, "Email address does not match any key id");
            return;
        }

        if(server.getRegistrationStateByEmail(email) != null) {
            sendError(connection, "Outstanding request already exists for email address "+ email);
            return;
        }

        final KeyRegistrationState registrationState = server.createRegistrationState(email, keyUtils.getPublicKeyRing());
        connection.writeMessage(KeyRegistrationResponse.createSuccessResponse(registrationState.getRequestId()));
        server.getRegistrationMailer().queueRequest(registrationState);
    }

    private boolean emailMatchesUserIDList(String email, List<String> uids) {
        for(String id: uids) {
            if(emailMatchesUserID(email, id)) {
                return true;
            }
        }
        return false;
    }

    private boolean emailMatchesUserID(String email, String uid) {
        if(uid.equalsIgnoreCase(email)) {
            return true;
        }
        if(uid.contains("<"+ email + ">")) {
            return true;
        }
        return false;
    }

    private boolean isValidEmailAddress(String email) {
        // TODO

        return true;

    }

    private void sendError(ConnectionTask connection, String message) {
        connection.writeMessage(KeyRegistrationResponse.createErrorResponse(message));
    }
}
