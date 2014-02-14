package com.subgraph.sgmail.model;


public class StoredRootPreferences extends StoredPreferences {
	public static StoredRootPreferences create() {
		final StoredRootPreferences prefs = new StoredRootPreferences();
		Preferences.initializeRootPreferences(prefs);
		return prefs;
	}
}
