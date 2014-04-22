package com.subgraph.sgmail.internal;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.subgraph.sgmail.IRandom;

public class RandomService implements IRandom {
	private final static Logger logger = Logger.getLogger(RandomService.class.getName());
	private final static boolean useNative = false;
	
	private final SecureRandom secureRandom;

	public RandomService() {
		this.secureRandom = createSecureRandomInstance();
	}

	private SecureRandom createSecureRandomInstance() {
		try {
			if (useNative) {
				return SecureRandom.getInstance("NativePRNG");
			} else {
				return SecureRandom.getInstanceStrong();
			}
		} catch (NoSuchAlgorithmException e) {
			logger.log(Level.SEVERE, "Failed to create SecureRandom instance "+ e, e);
		}
		return null;
	}

	@Override
	public SecureRandom getSecureRandom() {
		return secureRandom;
	}

}
