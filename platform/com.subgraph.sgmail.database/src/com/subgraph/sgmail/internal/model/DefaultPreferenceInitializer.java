package com.subgraph.sgmail.internal.model;

import com.subgraph.sgmail.database.Preferences;

public class DefaultPreferenceInitializer {

  static void initializeRootPreferences(Preferences preferences) {
    new DefaultPreferenceInitializer(preferences).initializeRootDefaults();
  }

  static void initializeAccountPreferences(Preferences preferences) {
    new DefaultPreferenceInitializer(preferences).initializeAccountDefaults();
  }

  private final Preferences preferences;

  private DefaultPreferenceInitializer(Preferences preferences) {
    this.preferences = preferences;
  }

  private void initializeRootDefaults() {
    set(Preferences.IMAP_DEBUG_OUTPUT, false);
    set(Preferences.DUMP_SELECTED_MESSAGE, false);
    set(Preferences.FETCH_KEYS_FROM_IDENTITY_SERVER, true);
    set(Preferences.IDENTITY_SERVER_ADDRESS, "pkx53pmulhqw3wkt.onion:12345");
    set(Preferences.TOR_ENABLED, "false");
    set(Preferences.TOR_SOCKS_PORT, "9050");
  }

  private void initializeAccountDefaults() {
    set(Preferences.ACCOUNT_DEFAULT_ENCRYPT, true);
    set(Preferences.ACCOUNT_DEFAULT_SIGN, true);
  }

  private void set(String name, boolean value) {
    preferences.setPreferenceDefault(name, value);
  }

  private void set(String name, String value) {
    preferences.setPreferenceDefaultValue(name, value);
  }
}
