package com.subgraph.sgmail.model;


import com.db4o.activation.ActivationPurpose;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.subgraph.sgmail.events.ContactPublicIdentityChangedEvent;
import com.subgraph.sgmail.identity.PublicIdentity;
import com.subgraph.sgmail.identity.client.KeyLookupResult;

import java.util.Date;
import java.util.logging.Logger;

public class Contact extends AbstractActivatable {
    private final static Logger logger = Logger.getLogger(Contact.class.getName());

    private final String emailAddress;

    private StoredPublicIdentity publicIdentity;
    private long notFoundAt;

    private transient boolean fetchInProgress;

    public Contact(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailAddress() {
        activate(ActivationPurpose.READ);
        return emailAddress;
    }

    public StoredPublicIdentity getPublicIdentity() {
        activate(ActivationPurpose.READ);
        return publicIdentity;
    }

    public synchronized void fetchPublicIdentity() {
        if(fetchInProgress) {
            return;
        }
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
                processKeyLookupResult(keyLookupResult);
            }

            @Override
            public void onFailure(Throwable throwable) {
                endFetch();
                throwable.printStackTrace();
                logger.warning("Error processing key lookup "+ throwable);
            }
        };
    }

    private void processKeyLookupResult(KeyLookupResult result) {
        endFetch();
        if(result.isErrorResult()) {
            logger.warning("Key lookup failed for identity "+ emailAddress + " : "+ result.getErrorMessage());
            return;
        } else if (result.isNotFoundResult()) {
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
}
