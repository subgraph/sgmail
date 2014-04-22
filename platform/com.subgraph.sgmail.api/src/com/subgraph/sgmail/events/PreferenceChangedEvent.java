package com.subgraph.sgmail.events;

import com.subgraph.sgmail.database.Preferences;

public class PreferenceChangedEvent {
	private final Preferences preferences; 
	private final String name;
	private final String oldValue;
	private final String newValue;
	
	public PreferenceChangedEvent(Preferences preferences, String name, String oldValue, String newValue) {
		this.preferences = preferences;
		this.name = name;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}
	
	public Preferences getPreferences() {
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
