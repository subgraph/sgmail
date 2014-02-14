package com.subgraph.sgmail.model;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.events.*;
import com.db4o.query.Predicate;
import com.db4o.ta.DeactivatingRollbackStrategy;
import com.db4o.ta.TransparentPersistenceSupport;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.subgraph.sgmail.events.AccountAddedEvent;
import com.subgraph.sgmail.events.PreferenceChangedEvent;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.identity.PublicIdentity;
import com.subgraph.sgmail.identity.PublicIdentityCache;
import com.subgraph.sgmail.identity.client.IdentityServerManager;
import com.subgraph.sgmail.sync.SynchronizationManager;

import javax.mail.Session;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;

public class Model {

	private final static Logger logger = Logger.getLogger(Model.class.getName());
	
	private final static String DATABASE_FILENAME = "mail.db";
	
	private final File databaseDirectory;
	private final Session session;
	private final EventBus eventBus;
	private final PublicIdentityCache publicIdentityCache;
	private final Object dbLock = new Object();
	
	private SynchronizationManager synchronizationManager;
	private IdentityServerManager identityServerManager;
    private ListeningExecutorService executor;

	private boolean isOpened;
	private ObjectContainer db;

    private Map<String, Contact> temporaryContactMap = new HashMap<>();

	public Model(File databaseDirectory) {
		this(databaseDirectory, new Properties());
	}

	public Model(File databaseDirectory, Properties sessionProperties) {
		this.databaseDirectory = checkNotNull(databaseDirectory);
		this.session = Session.getInstance(sessionProperties);
		this.eventBus = new EventBus();
		eventBus.register(this);
		this.publicIdentityCache = new PublicIdentityCache(this);
	}

	public synchronized SynchronizationManager getSynchronizationManager() {
		if(synchronizationManager == null) {
			synchronizationManager = new SynchronizationManager(this);
		}
		return synchronizationManager;
	}

    public synchronized IdentityServerManager getIdentityServerManager() {
        if(identityServerManager == null) {
            identityServerManager = new IdentityServerManager(this);
        }
        return identityServerManager;
    }

	@Subscribe
	public void onPreferenceChanged(PreferenceChangedEvent event) {
		if(event.isPreferenceName(Preferences.IMAP_DEBUG_OUTPUT)) {
			boolean flag = event.getPreferences().getBoolean(Preferences.IMAP_DEBUG_OUTPUT);
			session.setDebug(flag);
		}
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
		if(account instanceof IMAPAccount) {
            final IMAPAccount imapAccount = (IMAPAccount) account;
            db.store(imapAccount.getSmtpAccount());
            imapAccount.onActivate(this);
		}
        db.store(account);
        db.commit();
        System.out.println("account added in model.addAccount()");
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

    public synchronized ListeningExecutorService getExecutor() {
        if(executor == null) {
            executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        }
        return executor;
    }

    public <T> ListenableFuture<T> submitTask(Callable<T> task) {
        return getExecutor().submit(task);
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


    public Contact getContactByEmailAddress(final String emailAddress) {
        final ObjectSet<Contact> result = db.query(new Predicate<Contact>() {
            @Override
            public boolean match(Contact contact) {
                return contact.getEmailAddress().equalsIgnoreCase(emailAddress);
            }
        });

        if(result.size() == 0) {
            return getTemporaryContactByEmailAddress(emailAddress);
        } else if(result.size() > 1) {
            logger.warning("Multiple Contact entries found for emailAddress = "+ emailAddress + " ignoring duplicates");
        }
        return result.get(0);
    }

    private Contact getTemporaryContactByEmailAddress(String emailAddress) {
        synchronized (temporaryContactMap) {
            if(!temporaryContactMap.containsKey(emailAddress)) {
                Contact contact = new Contact(emailAddress);
                contact.onActivate(this);
                temporaryContactMap.put(emailAddress, contact);
            }
            return temporaryContactMap.get(emailAddress);
        }
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
	
	public PrivateIdentity findPrivateIdentity(String email) {
		return publicIdentityCache.findPrivateKey(email);
	}

	public List<PrivateIdentity> getLocalPrivateIdentities() {
		return publicIdentityCache.getLocalPrivateIdentities();
	}

	public StoredUserInterfaceState getStoredUserInterfaceState() {
		final StoredUserInterfaceState result = getModelSingleton(StoredUserInterfaceState.class);
		return (result != null) ? (result) 
				: (storeNewObject(new StoredUserInterfaceState()));
	}
	
	public StoredPreferences getRootStoredPreferences() {
		final StoredPreferences result = getModelSingleton(StoredRootPreferences.class);
		return (result != null) ? (result) 
				: (storeNewObject(StoredRootPreferences.create()));
	}

	public byte[] findAvatarImageDataForEmail(String emailAddress) {
		for(PublicIdentity pk: findIdentitiesFor(emailAddress)) {
			byte[] imageBytes = pk.getImageData();
			if(imageBytes != null && imageBytes.length > 0) {
				return imageBytes;
			}
		}
		return null;
	}

	private <T extends AbstractActivatable> T storeNewObject(T newObject) {
		newObject.onActivate(this);
		db.store(newObject);
		return newObject;
	}

	private <T> T getModelSingleton(Class<T> klass) {
		checkOpened();
		final ObjectSet<T> result = db.query(klass);
		if(result.size() == 0) {
			return null;
		} else if(result.size() > 1) {
			logger.warning("Found multiple instances of singleton class: "+ klass.getName() +" ignoring duplicates");
		}
		return result.get(0);
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
