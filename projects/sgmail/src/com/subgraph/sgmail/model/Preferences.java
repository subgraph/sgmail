package com.subgraph.sgmail.model;

public class Preferences {
	public static String ACCOUNT_DEFAULT_SIGN = "com.subgraph.sgmail.account.defaultSign";
	public static String ACCOUNT_DEFAULT_ENCRYPT = "com.subgraph.sgmail.account.defaultEncrypt";
	public static String IMAP_DEBUG_OUTPUT = "com.subgraph.sgmail.imap.debugOutput";
	public static String DUMP_SELECTED_MESSAGE = "com.subgraph.sgmail.debug.dumpMessages";
	public static String FETCH_KEYS_FROM_IDENTITY_SERVER = "com.subgraph.sgmail.autofetchKeys";
	
	public static String IDENTITY_SERVER_ADDRESS = "com.subgraph.sgmail.identity.serverAddress";
	
	
	public static void initializeRootPreferences(StoredPreferences preferences) {
		final Preferences initializer = new Preferences(preferences);
		initializer.initializeRootDefaults();
	}
	
	
	public static void initializeAccountPreferences(StoredPreferences preferences) {
		final Preferences initializer = new Preferences(preferences);
		initializer.initializeAccountDefaults();
	}
	
	private final StoredPreferences preferences;
	
	private Preferences(StoredPreferences prefs) {
		this.preferences = prefs;
	}
	
	private void initializeRootDefaults() {
		set(IMAP_DEBUG_OUTPUT, false);
		set(DUMP_SELECTED_MESSAGE, false);
		set(FETCH_KEYS_FROM_IDENTITY_SERVER, true);
		set(IDENTITY_SERVER_ADDRESS, "127.0.0.1:12345");
	}
	
	private void initializeAccountDefaults() {
		set(ACCOUNT_DEFAULT_ENCRYPT, true);
		set(ACCOUNT_DEFAULT_SIGN, true);
	}
	
	private void set(String name, boolean value) {
		preferences.setPreferenceDefault(name, value);
	}
	
	private void set(String name, String value) {
		preferences.setPreferenceDefaultValue(name, value);
	}
}
