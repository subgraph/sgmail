package com.subgraph.sgmail.model;


import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Logger;

import javax.mail.internet.InternetAddress;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.identity.PublicIdentity;

 class Contact {
    private final static Logger logger = Logger.getLogger(Contact.class.getName());

    private final String emailAddress;
    private String personal;

    private PublicIdentity publicIdentity;
    private long notFoundAt;

    private transient boolean fetchInProgress;

    public Contact(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Contact(String emailAddress, String personal) {
        this.emailAddress = emailAddress;
        this.personal = personal;
    }

    public InternetAddress toInternetAddress() throws UnsupportedEncodingException {
        //activate(ActivationPurpose.READ);
        return new InternetAddress(emailAddress, personal);
    }

    public String getEmailAddress() {
       // activate(ActivationPurpose.READ);
        return emailAddress;
    }

    public String getPersonal() {
       // activate(ActivationPurpose.READ);
        return personal;
    }

    public PublicIdentity getPublicIdentity() {
        //activate(ActivationPurpose.READ);
        return publicIdentity;
    }

    public List<PublicIdentity> getLocalPublicKeys() {
        //activate(ActivationPurpose.READ);
        // XXX fix me with new service
        //return model.findBestIdentitiesFor(emailAddress);
        return null;
    }
/*
    public synchronized void fetchPublicIdentity() {
        if(fetchInProgress) {
            return;
        }
        logger.info("Querying identity server for "+ emailAddress);
        final ListenableFuture<KeyLookupResult> future = model.getIdentityServerManager().lookupKeyByEmailAddress(emailAddress);
        Futures.addCallback(future, createCallback());
    }

    private synchronized void endFetch() {
        fetchInProgress = false;
    }

    private FutureCallback<KeyLookupResult> createCallback() {
        return new FutureCallback<KeyLookupResult>() {
            @Override
            public void onSuccess(KeyLookupResult keyLookupResult) {
                logger.info("Response received from lookup of address "+ emailAddress);
                processKeyLookupResult(keyLookupResult);
            }

            @Override
            public void onFailure(Throwable throwable) {
                endFetch();
                throwable.printStackTrace();
                logger.warning("Error processing key lookup "+ throwable.getMessage());
            }
        };
    }

    private void processKeyLookupResult(KeyLookupResult result) {
        endFetch();
        if(result.isErrorResult()) {
            logger.warning("Key lookup failed for identity "+ emailAddress + " : "+ result.getErrorMessage());
            return;
        } else if (result.isNotFoundResult()) {
            logger.info("No result found for "+ emailAddress);
            activate(ActivationPurpose.WRITE);
            notFoundAt = new Date().getTime();
            return;
        }
        setPublicIdentityFromBytes(result.getKeyData(), PublicIdentity.KEY_SOURCE_IDENTITY_SERVER);
    }

    private void setPublicIdentityFromBytes(byte[] keyData, int keySource) {
        activate(ActivationPurpose.WRITE);
        final StoredPublicIdentity oldIdentity = publicIdentity;
        publicIdentity = new StoredPublicIdentity(keyData, keySource);
        model.store(this);
        model.store(publicIdentity);
        model.commit();
        model.postEvent(new ContactPublicIdentityChangedEvent(this, oldIdentity));
        if(oldIdentity != null) {
            model.delete(oldIdentity);
        }
    }
    */
}