package com.subgraph.sgmail.internal.directory;

import com.subgraph.sgmail.directory.protocol.KeyRegistrationRequest;
import com.subgraph.sgmail.directory.protocol.KeyRegistrationResponse;
import com.subgraph.sgmail.directory.protocol.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

public class KeyRegistrationHandler implements MessageHandler {
	
    private final static KeyFingerPrintCalculator keyFingerprintCalculator = new BcKeyFingerprintCalculator();

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
        final PGPPublicKeyRing publicKeyRing = createPublicKeyRing(keyData);
        final List<String> uids = getUserIDs(publicKeyRing);
        
        
        if(!emailMatchesUserIDList(email, uids)) {
            sendError(connection, "Email address does not match any key id");
            return;
        }

        if(server.getRegistrationStateByEmail(email) != null) {
            sendError(connection, "Outstanding request already exists for email address "+ email);
            return;
        }

        final KeyRegistrationState registrationState = server.createRegistrationState(email, publicKeyRing);
        connection.writeMessage(KeyRegistrationResponse.createSuccessResponse(registrationState.getRequestId()));
        server.getRegistrationMailer().queueRequest(registrationState);
    }
    
    private PGPPublicKeyRing createPublicKeyRing(byte[] keyData) throws IOException {
    	return new PGPPublicKeyRing(keyData, keyFingerprintCalculator);
    }

    private List<String> getUserIDs(PGPPublicKeyRing publicKeyRing) {
    	final PGPPublicKey master = publicKeyRing.getPublicKey();
    	final List<String> uids = new ArrayList<>();
    	final Iterator<?> it = master.getUserIDs();
    	while(it.hasNext()) {
    		uids.add((String) it.next());
    	}
    	return uids;
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
