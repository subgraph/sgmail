package com.subgraph.sgmail.identity;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Random {
    private final static Logger logger = Logger.getLogger(Random.class.getName());

    private final static Random INSTANCE = new Random();

    public static Random getInstance() {
        return INSTANCE;
    }

    private final static boolean useNative = false;

    private final SecureRandom secureRandom;

    private Random() {
        this.secureRandom = createSecureRandomInstance();
    }

    private SecureRandom createSecureRandomInstance() {
        try {
            if(useNative) {
                return SecureRandom.getInstance("NativePRNG");
            } else {
                return SecureRandom.getInstanceStrong();
            }
        } catch (NoSuchAlgorithmException e) {
            logger.log(Level.SEVERE, "Failed to create SecureRandom instance "+ e, e);
        }
        return null;
    }

    public SecureRandom getSecureRandom() {
        return secureRandom;
    }

}
