package com.subgraph.sgmail.identity;

import com.google.common.base.Strings;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.bcpg.attr.ImageAttribute;
import org.bouncycastle.openpgp.*;
import org.bouncycastle.openpgp.operator.KeyFingerPrintCalculator;
import org.bouncycastle.openpgp.operator.PBESecretKeyDecryptor;
import org.bouncycastle.openpgp.operator.PGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.bc.BcPBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.bc.BcPGPDigestCalculatorProvider;

import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

public class OpenPGPKeyUtils {

    private final static Logger logger = Logger.getLogger(OpenPGPKeyUtils.class.getName());
    private final static KeyFingerPrintCalculator keyFingerprintCalculator = new BcKeyFingerprintCalculator();

    public static OpenPGPKeyUtils createFromPublicKeyBytes(byte[] keyData) throws IOException {
        return new OpenPGPKeyUtils(bytesToPublicKeyRing(keyData));
    }

    public static OpenPGPKeyUtils createFromKeyBytes(byte[] publicKeyData, byte[] privateKeyData, String passphrase) throws IOException, PGPException {
        return new OpenPGPKeyUtils(bytesToPublicKeyRing(publicKeyData), bytesToSecretKeyRing(privateKeyData), passphrase);
    }

    private static PGPPublicKeyRing bytesToPublicKeyRing(byte[] keyData) throws IOException {
        return new PGPPublicKeyRing(keyData, keyFingerprintCalculator);
    }

    private static PGPSecretKeyRing bytesToSecretKeyRing(byte[] keyData) throws IOException, PGPException {
        return new PGPSecretKeyRing(keyData, keyFingerprintCalculator);
    }
    private final PGPPublicKeyRing publicKeyRing;
    private final PGPSecretKeyRing secretKeyRing;
    private final PBESecretKeyDecryptor secretKeyDecryptor;

    private List<String> cachedUserIDs;
    private List<PGPPublicKey> cachedPublicKeys;
    private byte[] cachedImageData;

    public OpenPGPKeyUtils(PGPPublicKeyRing publicKeyRing) {
        this(publicKeyRing, null, null);
    }

    public OpenPGPKeyUtils(PGPPublicKeyRing publicKeyRing, PGPSecretKeyRing secretKeyRing, String passphrase) {
        this.publicKeyRing = publicKeyRing;
        this.secretKeyRing = secretKeyRing;
        this.secretKeyDecryptor = createSecretKeyDecryptor(passphrase);
    }

    private PBESecretKeyDecryptor createSecretKeyDecryptor(String passphrase) {
        final char[] passphraseChars = Strings.nullToEmpty(passphrase).toCharArray();
        return new BcPBESecretKeyDecryptorBuilder(new BcPGPDigestCalculatorProvider()).build(passphraseChars);
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

    public void addImageAttribute(byte[] imageData) throws PGPException, SignatureException {
        final PGPPublicKey nKey = addImageAttributePacket(secretKeyRing.getSecretKey(), createImageAttributePacket(imageData));
        PGPPublicKeyRing.insertPublicKey(publicKeyRing, nKey);
        PGPSecretKeyRing.replacePublicKeys(secretKeyRing, publicKeyRing);
        cachedImageData = null;
    }

    private PGPPublicKey addImageAttributePacket(PGPSecretKey secretKey, PGPUserAttributeSubpacketVector imageAttributePacket) throws PGPException, SignatureException {
        final PGPPublicKey publicKey = secretKey.getPublicKey();
        final PGPSignatureGenerator sigGen = createSignatureGenerator(secretKey, PGPSignature.POSITIVE_CERTIFICATION);
        final PGPSignature signature = sigGen.generateCertification(imageAttributePacket, publicKey);
        return PGPPublicKey.addCertification(publicKey, imageAttributePacket, signature);
    }

    private PGPSignatureGenerator createSignatureGenerator(PGPSecretKey secretKey, int signatureType) throws PGPException {
        final PGPPrivateKey privateKey = secretKey.extractPrivateKey(secretKeyDecryptor);
        final PGPContentSignerBuilder csb = new BcPGPContentSignerBuilder(secretKey.getPublicKey().getAlgorithm(), HashAlgorithmTags.SHA1);
        final PGPSignatureGenerator sigGen = new PGPSignatureGenerator(csb);
        sigGen.init(signatureType, privateKey);
        return sigGen;
    }

    private PGPUserAttributeSubpacketVector createImageAttributePacket(byte[] imageData) {
        final PGPUserAttributeSubpacketVectorGenerator vGen = new PGPUserAttributeSubpacketVectorGenerator();
        vGen.setImageAttribute(ImageAttribute.JPEG, imageData);
        return vGen.generate();
    }
}
