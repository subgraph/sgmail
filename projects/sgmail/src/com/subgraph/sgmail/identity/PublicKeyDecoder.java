package com.subgraph.sgmail.identity;


import org.bouncycastle.bcpg.attr.ImageAttribute;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPUserAttributeSubpacketVector;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class PublicKeyDecoder {
    private final static Logger logger = Logger.getLogger(PublicKeyDecoder.class.getName());
    private final static KeyFingerPrintCalculator keyFingerprintCalculator = new BcKeyFingerprintCalculator();

    public static PublicKeyDecoder createFromBytes(byte[] keyData) throws IOException {
        final PGPPublicKeyRing pkr = new PGPPublicKeyRing(keyData, keyFingerprintCalculator);
        return new PublicKeyDecoder(pkr);
    }

    private final PGPPublicKeyRing publicKeyRing;

    private List<String> cachedUserIDs;
    private List<PGPPublicKey> cachedPublicKeys;
    private byte[] cachedImageData;

    public PublicKeyDecoder(PGPPublicKeyRing publicKeyRing) {
        this.publicKeyRing = publicKeyRing;
    }

    public PGPPublicKeyRing getPublicKeyRing() {
        return publicKeyRing;
    }

    public synchronized List<String> getUserIDs() {
        if(cachedUserIDs == null) {
            cachedUserIDs = generateUserIDs();
        }
        return cachedUserIDs;

    }

    public synchronized List<PGPPublicKey> getPublicKeys() {
        if(cachedPublicKeys == null) {
            cachedPublicKeys = generatePublicKeys();
        }
        return cachedPublicKeys;
    }

    public synchronized byte[] getImageData() {
        if(cachedImageData == null) {
            cachedImageData = generateImageData();
        }
        return cachedImageData;
    }

    private List<String> generateUserIDs() {
        final PGPPublicKey master = publicKeyRing.getPublicKey();
        final List<String> uids = new ArrayList<>();
        final Iterator<?> it = master.getUserIDs();
        while(it.hasNext()) {
            uids.add((String) it.next());
        }
        return uids;
    }

    private List<PGPPublicKey> generatePublicKeys() {
        final List<PGPPublicKey> keys = new ArrayList<>();
        final Iterator<?> it = publicKeyRing.getPublicKeys();
        while(it.hasNext()) {
            keys.add((PGPPublicKey) it.next());
        }
        return keys;
    }

    private byte[] generateImageData() {
        final PGPPublicKey master = publicKeyRing.getPublicKey();
        final Iterator<?> it = master.getUserAttributes();
        while(it.hasNext()) {
            PGPUserAttributeSubpacketVector uasv = (PGPUserAttributeSubpacketVector) it.next();
            ImageAttribute image = uasv.getImageAttribute();
            if(image != null) {
                return image.getImageData();
            }
        }
        return new byte[0];
    }
}
