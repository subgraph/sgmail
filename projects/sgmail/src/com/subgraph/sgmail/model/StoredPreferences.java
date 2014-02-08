package com.subgraph.sgmail.model;

import java.util.Map;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableHashMap;
import com.subgraph.sgmail.events.PreferenceChangedEvent;

public class StoredPreferences extends AbstractActivatable {
	
	private final Map<String, String> preferences = new ActivatableHashMap<>();
	private final Map<String, String> defaultPreferences = new ActivatableHashMap<>();
	
	public boolean contains(String name) {
		activate(ActivationPurpose.READ);
		return preferences.containsKey(name) || defaultPreferences.containsKey(name);
	}
	
	public void unsetPreference(String name) {
		activate(ActivationPurpose.READ);
		if(preferences.containsKey(name)) {
			final String old = preferences.remove(name);
			model.postEvent(new PreferenceChangedEvent(this, name, old, null));
		}
	}

	public String getPreference(String name) {
		activate(ActivationPurpose.READ);
		if(preferences.containsKey(name)) {
			return preferences.get(name);
		} {
			return defaultPreferences.get(name);
		}
	}
	
	public String getPreferenceDefault(String name) {
		activate(ActivationPurpose.READ);
		return defaultPreferences.get(name);
	}
	
	public void setPreferenceValue(String name, String value) {
		activate(ActivationPurpose.READ);
		final String old = preferences.get(name);
		preferences.put(name, value);
		model.postEvent(new PreferenceChangedEvent(this, name, old, value));
	}

	public void setPreferenceDefaultValue(String name, String value) {
		activate(ActivationPurpose.READ);
		defaultPreferences.put(name, value);
	}
	
	public void setPreferenceValue(String name, boolean value) {
		setPreferenceValue(name, value ? "true" : "false");
	}
	
	public void setPreferenceDefault(String name, boolean value) {
		setPreferenceDefaultValue(name, value ? "true" : "false");
	}
	
	public void setPreferenceValue(String name, int value) {
		setPreferenceValue(name, Integer.toString(value));
	}
	
	public void setPreferenceDefault(String name, int value) {
		setPreferenceDefaultValue(name, Integer.toString(value));
	}
	
	public int getInteger(String name) {
		return getIntegerValue(getPreference(name));
	}
	
	public int getIntegerDefault(String name) {
		return getIntegerValue(getPreferenceDefault(name));
	}

	private static int getIntegerValue(String value) {
		if(value == null) {
			return 0;
		}
		try {
			return new Integer(value).intValue();
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	public boolean getBoolean(String name) {
		return getBooleanValue(getPreference(name));
	}
	
	public boolean getBooleanDefault(String name) {
		return getBooleanValue(getPreferenceDefault(name));
	}
	
	private static boolean getBooleanValue(String value) {
		if(value == null) {
			return false;
		} else {
			return value.equals("true");
		}
	}
	
}
