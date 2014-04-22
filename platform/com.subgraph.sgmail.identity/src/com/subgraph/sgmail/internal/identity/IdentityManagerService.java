package com.subgraph.sgmail.internal.identity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.db4o.query.Predicate;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.subgraph.sgmail.IRandom;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.identity.Contact;
import com.subgraph.sgmail.identity.IdentityManager;
import com.subgraph.sgmail.identity.KeyGenerationParameters;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.identity.PublicIdentity;

public class IdentityManagerService implements IdentityManager {

	private final Map<String, Contact> temporaryContactMap = new HashMap<>();
	
	private PublicIdentityCache publicIdentityCache;
	private Database database;
	private IRandom random;
	private ListeningExecutorService executor;
	
	public void activate() {
		publicIdentityCache = new PublicIdentityCache(database);
	}
	
	public void deactivate() {
		publicIdentityCache = null;
	}
	
	public void setRandom(IRandom random) {
		this.random = random;
	}
	
	public void setDatabase(Database database) {
		this.database = database;
	}
	
	public void setExecutor(ListeningExecutorService executor) {
		this.executor = executor;
	}
	
	@Override
	public List<PublicIdentity> findPublicKeysByAddress(String emailAddress) {
		return publicIdentityCache.findKeysFor(emailAddress);
	}

	@Override
	public List<PrivateIdentity> getLocalPrivateIdentities() {
		return publicIdentityCache.getLocalPrivateIdentities();
	}

	@Override
	public PrivateIdentity findPrivateKeyByAddress(String emailAddress) {
		return publicIdentityCache.findPrivateKey(emailAddress);
	}

	@Override
	public Contact getContactByEmailAddress(String emailAddress) {
		Contact c = database.getSingleByPredicate(new Predicate<Contact>() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean match(Contact contact) {
				return contact.getEmailAddress().equalsIgnoreCase(emailAddress);
			}
		});
		if(c != null) {
			return c;
		} else {
			return getTemporaryContactByEmailAddress(emailAddress);
		}
	}
	
	private Contact getTemporaryContactByEmailAddress(String emailAddress) {
		synchronized (temporaryContactMap) {
			if(!temporaryContactMap.containsKey(emailAddress)) {
				Contact contact = new ContactImpl(emailAddress);
				temporaryContactMap.put(emailAddress, contact);
			}
			return temporaryContactMap.get(emailAddress);
		}
	}

	@Override
	public KeyGenerationParameters createKeyGenerator() {
		return new KeyGenerationParametersImpl(random, executor);
	}
}
