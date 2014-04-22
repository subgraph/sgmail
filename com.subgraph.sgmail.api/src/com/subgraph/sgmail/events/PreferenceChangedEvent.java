package com.subgraph.sgmail.events;

import com.subgraph.sgmail.model.StoredPreferences;

public class PreferenceChangedEvent {
	private final StoredPreferences preferences; 
	private final String name;
	private final String oldValue;
	private final String newValue;
	
	public PreferenceChangedEvent(StoredPreferences preferences, String name, String oldValue, String newValue) {
		this.preferences = preferences;
		this.name = name;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	public StoredPreferences getPreferences() {
		return preferences;
	}

	public boolean isPreferenceName(String name) {
		return this.name.equals(name);
	}
	
	public String getName() {
		return name;
	}
	
	public String getOldValue() {
		return oldValue;
	}
	
	public String getNewValue() {
		return newValue;
	}

}
