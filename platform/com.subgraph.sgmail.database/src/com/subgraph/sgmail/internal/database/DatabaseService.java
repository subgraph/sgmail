package com.subgraph.sgmail.internal.database;

import gnu.trove.impl.hash.THash;
import gnu.trove.impl.hash.TIntHash;
import gnu.trove.impl.hash.TLongIntHash;
import gnu.trove.impl.hash.TPrimitiveHash;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongIntHashMap;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.diagnostic.DiagnosticToConsole;
import com.db4o.events.EventRegistry;
import com.db4o.events.EventRegistryFactory;
import com.db4o.query.Predicate;
import com.db4o.reflect.jdk.JdkReflector;
import com.db4o.ta.DeactivatingRollbackStrategy;
import com.db4o.ta.TransparentPersistenceSupport;
import com.google.common.collect.ImmutableSet;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Storeable;
import com.subgraph.sgmail.events.DatabaseOpenedEvent;
import com.subgraph.sgmail.internal.model.StoredPreferencesImpl;

public class DatabaseService implements Database {
	private final static Logger logger = Logger.getLogger(DatabaseService.class.getName());
	private final static List<Class<?>> CLASSES_WITH_TRANSIENT_FIELDS = Arrays.asList(
	            THash.class, TPrimitiveHash.class, TLongIntHash.class,
	            TLongIntHashMap.class, TIntObjectHashMap.class, TIntHash.class);
	
	private final static Set<String> MODEL_BUNDLE_NAMES = ImmutableSet.of(
			"gnu.trove", "com.subgraph.sgmail.messages", "com.subgraph.sgmail.api",
			"com.subgraph.sgmail.imap", "db4o_osgi");
	
	private final static boolean ENABLE_DIAGNOSTICS = false;
		
		
	private final static String DATABASE_FILENAME = "mail.db";
	private final Object dbLock = new Object();
	
	private IEventBus eventBus;
	private BundleContext bundleContext;
	
	private boolean isOpened;
	private ObjectContainer db;
	
	void activate(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
	}
	
	void deactivate() {
		
	}
	void setEventBus(IEventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	@Override
	public boolean open(File directory) {
		synchronized (dbLock) {
			checkClosed();
			directory.mkdirs();
			
			db = Db4oEmbedded.openFile(createConfiguration(), getDatabaseFilename(directory));
			registerEvents(db);
			isOpened = true;
			eventBus.post(new DatabaseOpenedEvent());
			return true;
		}
	}
	
	private EmbeddedConfiguration createConfiguration() {
		final EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
		config.common().add(new TransparentPersistenceSupport(new DeactivatingRollbackStrategy()));
		for(Class<?> clazz: CLASSES_WITH_TRANSIENT_FIELDS) {
			config.common().objectClass(clazz).storeTransientFields(true);
		}
		config.common().reflectWith(createReflector());
		if(ENABLE_DIAGNOSTICS) {
			config.common().diagnostic().addListener(new DiagnosticToConsole());
		}
		return config;
	}

	private JdkReflector createReflector() {
		final List<Bundle> bundleList = new ArrayList<Bundle>(MODEL_BUNDLE_NAMES.size());
		for(Bundle b: bundleContext.getBundles()) {
			if(MODEL_BUNDLE_NAMES.contains(b.getSymbolicName())) {
				bundleList.add(b);
			}
		}
		return new JdkReflector(new OsgiLoader(bundleList));
	}
	private String getDatabaseFilename(File directory) {
		final File dbFile = new File(directory, DATABASE_FILENAME);
		return dbFile.getPath();
	}
	
	@Override
	public void close() {
		synchronized (dbLock) {
			checkOpened();
			db.close();
			isOpened = false;
		}
	}
	
	@Override
	public <T> List<T> getAll(Class<T> clazz) {
		checkOpened();
		return db.query(clazz);
	}
	
	@Override
	public <T> List<T> getByPredicate(Predicate<T> predicate) {
		checkOpened();
		return db.query(predicate);
	}
	
	@Override 
	public <T> T getSingleByPredicate(Predicate<T> predicate) {
		final ObjectSet<T> result = db.query(predicate);
		if(result.size() == 0) {
			return null;
		} else if(result.size() > 1) {
			logger.warning("Multiple entries found for predicate query in getSingleByPredicate for type "+ result.get(0).getClass().getName());
		}
		return result.get(0);
	}

	@Override
	public <T> T getSingleton(Class<T> clazz) {
		checkOpened();
		synchronized(dbLock) {
			final ObjectSet<T> result = db.query(clazz);
			if(result.size() == 0) {
				return maybeCreate(clazz);
			} else if(result.size() > 1) {
				logger.warning("Found multiple instances of singleton class: "+ clazz.getName() +" ignoring duplicates");
			}
			return result.get(0);
		}
	}
	
	private <T> T maybeCreate(Class<T> clazz) {
		try {
			final Constructor<T> constructor = clazz.getConstructor();
			final T ob = constructor.newInstance();
			store(ob);
			return ob;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public void store(Object ob) {
		checkOpened();
		processObject(ob);
		db.store(ob);
	}

	@Override
	public void delete(Object ob) {
		checkOpened();
		db.delete(ob);;
	}

	@Override
	public void commit() {
		checkOpened();
		db.commit();
	}
	
	private void registerEvents(final ObjectContainer db) {
		final EventRegistry er = EventRegistryFactory.forObjectContainer(db);
		er.activated().addListener((events, eventArgs) -> onActivateObject(eventArgs.object()));
        er.created().addListener((events, eventArgs) -> onCreateObject(eventArgs.object()));
	}

	private void onActivateObject(Object ob) {
		processObject(ob);
	}

    private void onCreateObject(Object ob) {
    	processObject(ob);
    }

    private void processObject(Object ob) {
    	if(ob instanceof Storeable) {
    		((Storeable) ob).setDatabase(this);;
    	}
    	if(ob instanceof StoredPreferencesImpl) {
    		((StoredPreferencesImpl) ob).setEventBus(eventBus);
    	}
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
