package com.subgraph.sgmail.model;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Session;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.events.Event4;
import com.db4o.events.EventListener4;
import com.db4o.events.EventRegistry;
import com.db4o.events.EventRegistryFactory;
import com.db4o.events.ObjectInfoEventArgs;
import com.db4o.ta.DeactivatingRollbackStrategy;
import com.db4o.ta.TransparentPersistenceSupport;
import com.google.common.eventbus.EventBus;
import com.subgraph.sgmail.events.AccountAddedEvent;
import com.subgraph.sgmail.identity.PublicIdentity;
import com.subgraph.sgmail.identity.PublicIdentityCache;
import com.subgraph.sgmail.sync.SynchronizationManager;

public class Model {

	private final static Logger logger = Logger.getLogger(Model.class.getName());
	
	private final static String DATABASE_FILENAME = "mail.db";
	
	private final File databaseDirectory;
	private final Session session;
	private final EventBus eventBus;
	private final PublicIdentityCache publicIdentityCache;
	private final Object dbLock = new Object();
	
	private SynchronizationManager synchronizationManager;
	
	private boolean isOpened;
	private ObjectContainer db;
	
	
	public Model(File databaseDirectory) {
		this(databaseDirectory, new Properties());
	}

	public Model(File databaseDirectory, Properties sessionProperties) {
		this.databaseDirectory = checkNotNull(databaseDirectory);
		this.session = Session.getInstance(sessionProperties);
		this.eventBus = new EventBus();
		this.publicIdentityCache = new PublicIdentityCache(this);
	}

	public synchronized SynchronizationManager getSynchronizationManager() {
		if(synchronizationManager == null) {
			synchronizationManager = new SynchronizationManager(this);
		}
		return synchronizationManager;
	}

	public void enableSessionDebug() {
		session.setDebug(true);
	}

	public Session getSession() {
		return session;
	}
	
	public ObjectContainer getDatabase() {
		checkOpened();
		return db;
	}
	
	public void delete(Object ob) {
		checkOpened();
		db.delete(ob);
	}
	public void commit() {
		checkOpened();
		db.commit();
	}

	public void addAccount(Account account) {
		checkOpened();
		db.store(account);
		if(account instanceof IMAPAccount) {
			((IMAPAccount)account).onActivate(this);
		}
		eventBus.post(new AccountAddedEvent(account));
	}

	public void postEvent(Object event) {
		eventBus.post(event);
	}

	public void registerEventListener(Object listener) {
		eventBus.register(listener);
	}

	public void open() {
		synchronized(dbLock) {
			checkClosed();
			final EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
			config.common().add(new TransparentPersistenceSupport(new DeactivatingRollbackStrategy()));
			databaseDirectory.mkdirs();
			File dbFile = new File(databaseDirectory, DATABASE_FILENAME);
			db = Db4oEmbedded.openFile(config, dbFile.getPath());
			registerEvents(db);
			isOpened = true;
		}
	}
	
	public void close() {
		synchronized (dbLock) {
			checkOpened();
			if(synchronizationManager != null) {
				synchronizationManager.close();
			}
			db.close();
			isOpened = false;
		}
	}
	
	private void registerEvents(final ObjectContainer db) {
		final EventRegistry er = EventRegistryFactory.forObjectContainer(db);
		er.activated().addListener(new EventListener4<ObjectInfoEventArgs>() {
			@Override
			public void onEvent(Event4<ObjectInfoEventArgs> events,	ObjectInfoEventArgs eventArgs) {
				onActivateObject(eventArgs.object());
			}
		});
	}

	private void onActivateObject(Object ob) {
		if(ob instanceof AbstractActivatable) {
			((AbstractActivatable) ob).onActivate(this);
		}
	}
	
	public void store(Object ob) {
		checkOpened();
		if(ob instanceof AbstractActivatable) {
			AbstractActivatable aa = (AbstractActivatable) ob;
			aa.onActivate(this);
		}
		db.store(ob);
	}

	public List<Account> getAccounts() {
		checkOpened();
		return db.query(Account.class);
	}
	
	public List<PublicIdentity> findIdentitiesFor(String emailAddress) {
		return publicIdentityCache.findKeysFor(emailAddress);
	}
	
	public List<PublicIdentity> findBestIdentitiesFor(String emailAddress) {
		return publicIdentityCache.findBestKeysFor(emailAddress);
	}

	public List<StoredPublicIdentity> getStoredPublicIdentities() {
		checkOpened();
		return db.query(StoredPublicIdentity.class);
	}

	public StoredUserInterfaceState getStoredUserInterfaceState() {
		checkOpened();
		final ObjectSet<StoredUserInterfaceState> result = db.query(StoredUserInterfaceState.class);
		if(result.size() == 0) {
			return createNewStoredUserInterfaceState();
		} else if(result.size() > 1) {
			logger.warning("Found multiple StoredUserInterfaceState instances, ignoring duplicates");
		}
		return result.get(0);
	}

	private StoredUserInterfaceState createNewStoredUserInterfaceState() {
		final StoredUserInterfaceState state = new StoredUserInterfaceState();
		state.onActivate(this);
		db.store(state);
		return state;
	}

	private void checkClosed() {
		if(isOpened) {
			throw new IllegalStateException("Database is already opened");
		}
	}

	private void checkOpened() {
		synchronized(dbLock) {
			if(!isOpened) {
				throw new IllegalStateException("Model database is not opened.");
			}
		}
	}
}
