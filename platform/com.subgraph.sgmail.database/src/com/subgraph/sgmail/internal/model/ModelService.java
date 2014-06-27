package com.subgraph.sgmail.internal.model;

import com.db4o.query.Predicate;
import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.accounts.AccountList;
import com.subgraph.sgmail.database.ContactManager;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.database.Preferences;
import com.subgraph.sgmail.events.DatabaseOpenedEvent;

public class ModelService implements Model {
	
	private IEventBus eventBus;
	private Database database;
	
	private int modelVersion;

	void activate() {
		eventBus.register(this);
	}
	
	void deactivate() {
		eventBus.unregister(this);
	}
	
	@Subscribe
	public void onDatabaseOpened(DatabaseOpenedEvent event) {
		final ModelInformation modelInformation = database.getSingleton(ModelInformation.class);
		modelVersion = modelInformation.getModelVersion();
	}
	
	void setEventBus(IEventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	void setDatabase(Database database) {
		this.database = database;
	}
	
	@Override
	public int getModelVersion() {
		return modelVersion;
	}

	@Override
	public AccountList getAccountList() {
		return database.getSingleton(AccountListImpl.class);
	}

	@Override
	public StoredUserInterfaceStateImpl getStoredUserInterfaceState() {
		return database.getSingleton(StoredUserInterfaceStateImpl.class);
	}
	
	@Override
	public Database getDatabase() {
		return database;
	}

	@Override
	public synchronized Preferences getRootPreferences() {
		Preferences result = database.getSingleByPredicate(new Predicate<StoredPreferencesImpl>() {
			private static final long serialVersionUID = 1L;
			@Override
			public boolean match(StoredPreferencesImpl preferences) {
				return preferences.isRootPreferences();
			}
		});
		if(result != null) {
			return result;
		} else {
			return createNewRootPreferences();
		}
	}

	@Override
	public Preferences createNewAccountPreferences() {
		final Preferences preferences = new StoredPreferencesImpl(false);
		DefaultPreferenceInitializer.initializeAccountPreferences(preferences);
		return preferences;
	}
	
	@Override
	public int getNextUniqueId() {
		final UniqueIdGenerator uniqueIdGenerator = database.getSingleton(UniqueIdGenerator.class);
		return uniqueIdGenerator.next();
	}

	private Preferences createNewRootPreferences() {
		final Preferences preferences = new StoredPreferencesImpl(true);
		DefaultPreferenceInitializer.initializeRootPreferences(preferences);
		database.store(preferences);
		database.commit();
		return preferences;
	}

  @Override
  public ContactManager getContactManager() {
    return database.getSingleton(ContactManagerImpl.class);
  }
}
