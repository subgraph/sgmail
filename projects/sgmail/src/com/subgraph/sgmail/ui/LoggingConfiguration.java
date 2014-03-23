package com.subgraph.sgmail.ui;


import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class LoggingConfiguration {

    static public void configure() {
        String home = System.getProperty("user.home");
        configure(new File(home, ".sgos"));
    }
    static public void configure(File logDirectory) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT] %4$s: %5$s%6$s%n");
        try {
            final FileHandler fileHandler = new FileHandler(logDirectory.getPath() + File.separator + "sgmail.log.%g", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.FINEST);
            Logger.getLogger("").addHandler(fileHandler);
        } catch (IOException e) {
            Logger.getLogger(LoggingConfiguration.class.getName()).warning("IOException configuring logging "+ e);
        }
    }

    static public void close() {
        for (Handler handler : Logger.getLogger("").getHandlers()) {
            handler.close();
        }
    }
}
