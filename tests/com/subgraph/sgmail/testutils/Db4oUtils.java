package com.subgraph.sgmail.testutils;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.io.MemoryStorage;
import com.db4o.ta.DeactivatingRollbackStrategy;
import com.db4o.ta.TransparentPersistenceSupport;

public class Db4oUtils {
    private final static String MEMORY_DATABASE_FILENAME = "memory.db4o";
    private static int debugMessageLevel = 0;

    public static void setDebugMessageLevel(int value) {
        debugMessageLevel = value;
    }

    public static ObjectContainer openMemoryDatabase() {
        final EmbeddedConfiguration configuration = Db4oEmbedded.newConfiguration();
        final MemoryStorage memory = new MemoryStorage();
        configuration.file().storage(memory);
        configuration.common().add(new TransparentPersistenceSupport(new DeactivatingRollbackStrategy()));
        if(debugMessageLevel > 0) {
            configuration.common().messageLevel(debugMessageLevel);
        }
        return Db4oEmbedded.openFile(configuration, MEMORY_DATABASE_FILENAME);
    }

}
