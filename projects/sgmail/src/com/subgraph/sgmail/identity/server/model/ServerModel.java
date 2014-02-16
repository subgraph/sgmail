package com.subgraph.sgmail.identity.server.model;


import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;
import com.db4o.ta.DeactivatingRollbackStrategy;
import com.db4o.ta.TransparentPersistenceSupport;

import java.io.File;
import java.util.logging.Logger;

public class ServerModel {
    private final static Logger logger = Logger.getLogger(ServerModel.class.getName());
    private final static String DATABASE_FILENAME = "server.db";

    private final File databaseDirectory;

    private boolean isOpened;
    private final Object dbLock = new Object();
    private ObjectContainer db;

    public ServerModel(File databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
    }

    public void open() {
        synchronized (dbLock) {
            checkClosed();
            final EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
            config.common().add(new TransparentPersistenceSupport(new DeactivatingRollbackStrategy()));
            databaseDirectory.mkdirs();
            final File dbFile = new File(databaseDirectory, DATABASE_FILENAME);
            db = Db4oEmbedded.openFile(config, dbFile.getPath());

            isOpened = true;

        }
    }

    public void store(Object ob) {
        checkOpened();
        db.store(ob);
        db.commit();
    }

    public IdentityRecord findRecordForEmail(final String emailAddress) {
        checkOpened();
        ObjectSet<IdentityRecord> result = db.query(new Predicate<IdentityRecord>() {
            @Override
            public boolean match(IdentityRecord record) {
                return record.getEmailAddress().equals(emailAddress);
            }
        });
        if(result.isEmpty()) {
            return null;
        } else if(result.size() > 1) {
            logger.warning("Multiple identity records found for address "+ emailAddress);
        }
        return result.get(0);
    }

    private void checkOpened() {
        synchronized (dbLock) {
            if(!isOpened) {
                throw new IllegalStateException("model database not opened");
            }
        }
    }

    private void checkClosed() {
        if(isOpened) {
            throw new IllegalStateException("Database is already opened");
        }
    }
}

