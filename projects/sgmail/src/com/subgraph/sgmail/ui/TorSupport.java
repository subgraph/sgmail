package com.subgraph.sgmail.ui;


import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.events.PreferenceChangedEvent;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.Preferences;
import com.subgraph.sgmail.model.StoredPreferences;

import java.util.logging.Logger;

public class TorSupport {
    private final static Logger logger = Logger.getLogger(TorSupport.class.getName());

    public static TorSupport create(Model model) {
        final TorSupport ts = new TorSupport();
        model.registerEventListener(ts);
        ts.updateSocksConfiguration(model.getRootStoredPreferences());
        return ts;
    }

    @Subscribe
    public void onPreferenceChanged(PreferenceChangedEvent event) {
        if(!event.isPreferenceName(Preferences.TOR_ENABLED)) {
            return;
        }
        updateSocksConfiguration(event.getPreferences());
    }

    void updateSocksConfiguration(StoredPreferences preferences) {
        updateSocksConfiguration(
                preferences.getBoolean(Preferences.TOR_ENABLED),
                preferences.getInteger(Preferences.TOR_SOCKS_PORT));
    }

    void updateSocksConfiguration(boolean torEnabled, int socksPort) {
        if(!torEnabled) {
            logger.info("Disabling Tor SOCKS proxy support");
            System.getProperties().remove("socksProxyHost");
            System.getProperties().remove("socksProxyPort");
            return;
        }
        logger.info("Enabling Tor SOCKS proxy support on port "+ socksPort);
        System.getProperties().setProperty("socksProxyHost", "localhost");
        System.getProperties().setProperty("socksProxyPort", Integer.toString(socksPort));
    }
}
