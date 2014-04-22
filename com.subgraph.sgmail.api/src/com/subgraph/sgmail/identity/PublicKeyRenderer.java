package com.subgraph.sgmail.identity;


import com.google.common.base.Strings;
import com.google.common.primitives.UnsignedLongs;
import org.bouncycastle.openpgp.PGPPublicKey;

import java.text.SimpleDateFormat;

public class PublicKeyRenderer {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");

    private final PublicIdentity publicIdentity;

    public PublicKeyRenderer(PublicIdentity publicIdentity) {
        this.publicIdentity = publicIdentity;
    }

    public String renderPublicIdentity() {
        final StringBuilder sb = new StringBuilder();
        for(PGPPublicKey pk: publicIdentity.getPublicKeys()) {
            sb.append(renderPublicKey(pk));
            if(pk.isMasterKey()) {
                for(String uid: publicIdentity.getUserIds()) {
                    sb.append(renderUserIdLine(uid));
                }
            }
        }
        return sb.toString();
    }

    private String renderPublicKey(PGPPublicKey pk) {
        final StringBuilder sb = new StringBuilder();
        sb.append(pk.isMasterKey() ? "pub" : "sub");
        sb.append("  ");
        sb.append(renderPublicKeyInfo(pk));
        sb.append(" ");
        sb.append(dateFormat.format(pk.getCreationTime()));
        sb.append("\n");
        return sb.toString();
    }

    private String renderPublicKeyInfo(PGPPublicKey pk) {
        final StringBuilder sb = new StringBuilder();
        sb.append(pk.getBitStrength());
        sb.append(getAlgorithmLetter(pk.getAlgorithm()));
        sb.append("/");
        String longId = UnsignedLongs.toString(pk.getKeyID(), 16);
        int len = longId.length();
        String shortId = longId.substring(len - 8, len);
        sb.append(shortId);
        return sb.toString();
    }

    private String renderUserIdLine(String uid) {
        final StringBuilder sb = new StringBuilder();
        sb.append("uid");
        sb.append(Strings.repeat(" ", 17));
        sb.append(uid);
        sb.append("\n");
        return sb.toString();
    }

    private String getAlgorithmLetter(int algo) {
        switch(algo) {
            case PGPPublicKey.RSA_GENERAL:
                return "R";
            case PGPPublicKey.RSA_ENCRYPT:
                return "r";
            case PGPPublicKey.RSA_SIGN:
                return "s";
            case PGPPublicKey.ELGAMAL_ENCRYPT:
                return "g";
            case PGPPublicKey.ELGAMAL_GENERAL:
                return "G";
            case PGPPublicKey.DSA:
                return "D";
            case PGPPublicKey.ECDH:
                return "e";
            case PGPPublicKey.ECDSA:
                return "E";
            default:
                return "?";
        }
    }
}
