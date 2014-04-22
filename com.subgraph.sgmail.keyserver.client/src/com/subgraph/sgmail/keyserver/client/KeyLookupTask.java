package com.subgraph.sgmail.identity.client;


import com.subgraph.sgmail.identity.protocol.KeyLookupRequest;
import com.subgraph.sgmail.identity.protocol.KeyLookupResponse;
import com.subgraph.sgmail.identity.protocol.Message;
import com.subgraph.sgmail.identity.protocol.PublicKeyData;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class KeyLookupTask implements Callable<KeyLookupResult> {

    private final IdentityServerManager manager;
    private final String emailAddress;

    KeyLookupTask(IdentityServerManager manager, String emailAddress) {
        this.manager = manager;
        this.emailAddress = emailAddress;
    }

    @Override
    public KeyLookupResult call() throws Exception {
        final IdentityServerConnection connection = manager.getIdentityServerConnection();
        if(connection == null) {
            throw new IOException("Could not open connection to identity server");
        }
        final Message request = KeyLookupRequest.createLookupByEmail(emailAddress);
        return processResponse(connection.transact(request, KeyLookupResponse.class));
    }

    private KeyLookupResult processResponse(KeyLookupResponse response) {
        if(response.getKeys().isEmpty()) {
            return KeyLookupResult.createNotFoundResult();
        } else if(response.getKeys().size() > 1) {
            return KeyLookupResult.createErrorResult("Expecting single result and got multiple...");
        }
        final PublicKeyData pkd = response.getKeys().get(0);
        if(pkd.getKeyType() != PublicKeyData.PUBLIC_KEY_TYPE_IDENTITY_SERVER) {

        }

        return processPublicKeyBytes(pkd.getKeyBytes());
    }

    private KeyLookupResult processPublicKeyBytes(byte[] keyBytes) {
        final PGPPublicKeyRingCollection collection = readCollectionFromKeyBytes(keyBytes);
        final List<PGPPublicKeyRing> keyRings = getPublicKeyRingsFromCollection(collection);
        if(keyRings.isEmpty()) {
            return KeyLookupResult.createErrorResult("Empty keyring received from identity server");
        } else if(keyRings.size() > 1) {
            return KeyLookupResult.createErrorResult("Keyring containing multiple identities received");
        }
        final PGPPublicKeyRing pkr = keyRings.get(0);

        final List<String> ids = getUserIDsFromPublicKey(pkr.getPublicKey());

        for(String id: ids) {
            if(id.contains(emailAddress)) {
                return KeyLookupResult.createSuccessResult(pkr, keyBytes);
            }
        }
        return KeyLookupResult.createErrorResult("Key returned from identity server did not match address requested");
    }

    private PGPPublicKeyRingCollection readCollectionFromKeyBytes(byte[] keyBytes) {
        final ByteArrayInputStream input = new ByteArrayInputStream(keyBytes);
        try {
            return new PGPPublicKeyRingCollection(input);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PGPException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<PGPPublicKeyRing> getPublicKeyRingsFromCollection(PGPPublicKeyRingCollection collection) {
        final List<PGPPublicKeyRing> keyRings = new ArrayList<>();
        final Iterator<?> it = collection.getKeyRings();
        while(it.hasNext()) {
            keyRings.add((PGPPublicKeyRing) it.next());
        }
        return keyRings;
    }

    private List<String> getUserIDsFromPublicKey(PGPPublicKey pk) {
        final List<String> ids = new ArrayList<>();
        final Iterator<?> it = pk.getUserIDs();
        while(it.hasNext()) {
            ids.add((String) it.next());
        }
        return ids;
    }
}
