package com.subgraph.sgmail.directory.client;

import com.google.common.base.Charsets;
import com.google.common.primitives.UnsignedLongs;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.subgraph.sgmail.directory.protocol.KeyRegistrationFinalizeRequest;
import com.subgraph.sgmail.directory.protocol.KeyRegistrationFinalizeResponse;
import com.subgraph.sgmail.directory.protocol.KeyRegistrationRequest;
import com.subgraph.sgmail.directory.protocol.KeyRegistrationResponse;
import com.subgraph.sgmail.identity.PublicIdentity;

import javax.mail.internet.MimeMessage;

import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyRegistrationTask implements Callable<KeyRegistrationResult> {
    private final static Logger logger = Logger.getLogger(KeyRegistrationTask.class.getName());

    private final ListeningExecutorService executor;
    private final String emailAddress;
    private final PublicIdentity publicIdentity;
    private final ReceiveRegistrationEmailTask receiveRegistrationEmailTask;



    public KeyRegistrationTask(ListeningExecutorService executor, String emailAddress, PublicIdentity publicIdentity, String imapServer, String imapLogin, String imapPassword) {
    	this.executor = executor;
        this.emailAddress = emailAddress;
        this.publicIdentity = publicIdentity;
        this.receiveRegistrationEmailTask = new ReceiveRegistrationEmailTask(imapServer, imapLogin, imapPassword);
    }

    @Override
    public KeyRegistrationResult call() throws Exception {
        try {
            return runRegistration();
        } catch(Exception e) {
            logger.log(Level.WARNING, "Unhandled exception running key registration", e);
            throw e;
        }
    }

    private KeyRegistrationResult runRegistration() throws Exception {
        logger.fine("Starting key registration task");

        IdentityServerConnection connection = null;
        /*
        try {
            connection = model.getIdentityServerManager().getIdentityServerConnection();
        } catch(SocketException e) {
            if(e.getMessage() != null && e.getMessage().contains("Connection refused")) {
                return new KeyRegistrationResult("Failed to connect to identity server");
            } else {
                return new KeyRegistrationResult("SocketException connecting to identity server: "+ e.getMessage());
            }
        }
        */

        final KeyRegistrationRequest request = new KeyRegistrationRequest(publicIdentity.getPGPPublicKeyRing().getEncoded(), emailAddress);
        final ListenableFuture<MimeMessage> future = executor.submit(receiveRegistrationEmailTask);

        final KeyRegistrationResponse response = connection.transact(request, KeyRegistrationResponse.class);
        if(!response.isSuccess()) {
            future.cancel(true);
            return new KeyRegistrationResult(response.getErrorMessage());
        }
        receiveRegistrationEmailTask.setExpectedRequestId(response.getRequestId());

        final MimeMessage message = future.get();

        logger.fine("Email received in key registration task");

        final KeyRegistrationFinalizeRequest finalizeRequest = createFinalizeRequest(response.getRequestId(), message);
        final KeyRegistrationFinalizeResponse finalizeResponse = connection.transact(finalizeRequest, KeyRegistrationFinalizeResponse.class);
        logger.fine("Finalize response received in key registration task");
        if(finalizeResponse.isSuccess()) {
            return new KeyRegistrationResult();
        } else {
            return new KeyRegistrationResult(finalizeResponse.getErrorMessage());
        }
    }

    private KeyRegistrationFinalizeRequest createFinalizeRequest(long requestId, MimeMessage message) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        digest.update(UnsignedLongs.toString(requestId, 16).getBytes(Charsets.US_ASCII));
        final byte[] digestBytes = digest.digest();
        return new KeyRegistrationFinalizeRequest(requestId, digestBytes);
    }
}
