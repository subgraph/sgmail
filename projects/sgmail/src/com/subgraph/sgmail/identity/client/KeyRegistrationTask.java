package com.subgraph.sgmail.identity.client;

import com.google.common.base.Charsets;
import com.google.common.primitives.UnsignedLongs;
import com.google.common.util.concurrent.ListenableFuture;
import com.subgraph.sgmail.identity.PublicIdentity;
import com.subgraph.sgmail.identity.protocol.*;
import com.subgraph.sgmail.model.Model;

import javax.mail.internet.MimeMessage;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Callable;

public class KeyRegistrationTask implements Callable<KeyRegistrationResult> {

    private final Model model;
    private final String emailAddress;
    private final PublicIdentity publicIdentity;
    private final ReceiveRegistrationEmailTask receiveRegistrationEmailTask;



    public KeyRegistrationTask(Model model, String emailAddress, PublicIdentity publicIdentity, String imapServer, String imapLogin, String imapPassword) {
        this.model = model;

        this.emailAddress = emailAddress;
        this.publicIdentity = publicIdentity;
        this.receiveRegistrationEmailTask = new ReceiveRegistrationEmailTask(imapServer, imapLogin, imapPassword);
    }

    @Override
    public KeyRegistrationResult call() throws Exception {
        System.out.println("Starting key registration task");
        final IdentityServerConnection connection = model.getIdentityServerManager().getIdentityServerConnection();
        final KeyRegistrationRequest request = new KeyRegistrationRequest(publicIdentity.getPGPPublicKeyRing().getEncoded(), emailAddress);
        final ListenableFuture<MimeMessage> future = model.submitTask(receiveRegistrationEmailTask);

        final KeyRegistrationResponse response = connection.transact(request, KeyRegistrationResponse.class);
        if(!response.isSuccess()) {
            future.cancel(true);
            return new KeyRegistrationResult(response.getErrorMessage());
        }
        receiveRegistrationEmailTask.setExpectedRequestId(response.getRequestId());

        final MimeMessage message = future.get();

        System.out.println("Email received: "+ message);

        final KeyRegistrationFinalizeRequest finalizeRequest = createFinalizeRequest(response.getRequestId(), message);
        final KeyRegistrationFinalizeResponse finalizeResponse = connection.transact(finalizeRequest, KeyRegistrationFinalizeResponse.class);
        System.out.println("Finalize response received...");
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
