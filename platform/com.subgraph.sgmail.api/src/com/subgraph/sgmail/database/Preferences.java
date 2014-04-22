package com.subgraph.sgmail.database;

public interface Preferences {
	static final String ACCOUNT_DEFAULT_SIGN = "com.subgraph.sgmail.account.defaultSign";
	static final String ACCOUNT_DEFAULT_ENCRYPT = "com.subgraph.sgmail.account.defaultEncrypt";
	static final String IMAP_DEBUG_OUTPUT = "com.subgraph.sgmail.imap.debugOutput";
	static final String DUMP_SELECTED_MESSAGE = "com.subgraph.sgmail.debug.dumpMessages";
	static final String FETCH_KEYS_FROM_IDENTITY_SERVER = "com.subgraph.sgmail.autofetchKeys";
	
	static final String IDENTITY_SERVER_ADDRESS = "com.subgraph.sgmail.identity.serverAddress";

    static final String TOR_ENABLED = "com.subgraph.sgmail.torEnabled";
    static final String TOR_SOCKS_PORT = "com.subgraph.sgmail.torSocksPort";
	
	
	boolean contains(String name);
	void unsetPreference(String name);
	String getPreference(String name);
	String getPreferenceDefault(String name);
	void setPreferenceValue(String name, String value);
	void setPreferenceDefaultValue(String name, String value);
	void setPreferenceValue(String name, boolean value);
	void setPreferenceDefault(String name, boolean value);
	void setPreferenceValue(String name, int value);
	void setPreferenceDefault(String name, int value);
	int getInteger(String name);
	int getIntegerDefault(String name);
	boolean getBoolean(String name);
	boolean getBooleanDefault(String name);
}
