package com.subgraph.sgmail.internal.model;

import java.util.Map;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.collections.ActivatableHashMap;
import com.db4o.ta.Activatable;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.database.Preferences;
import com.subgraph.sgmail.events.PreferenceChangedEvent;

public class StoredPreferencesImpl implements Preferences, Activatable {
	private final Map<String, String> preferences = new ActivatableHashMap<>();
	private final Map<String, String> defaultPreferences = new ActivatableHashMap<>();
	
	private final boolean isRootPreferences;
	
	private transient IEventBus eventBus;
	private transient Activator activator;
	
	StoredPreferencesImpl(boolean isRootPreferences) {
		this.isRootPreferences = isRootPreferences;
	}
	
	public void setEventBus(IEventBus eventBus) {
		this.eventBus = eventBus;
	}
	
	public boolean isRootPreferences() {
		return isRootPreferences;
	}

	@Override
	public boolean contains(String name) {
		activate(ActivationPurpose.READ);
		return preferences.containsKey(name) || defaultPreferences.containsKey(name);
	}
	
	@Override
	public void unsetPreference(String name) {
		activate(ActivationPurpose.READ);
		if(preferences.containsKey(name)) {
			final String old = preferences.remove(name);
			eventBus.post(new PreferenceChangedEvent(this, name, old, null));
		}
	}

	@Override
	public String getPreference(String name) {
		activate(ActivationPurpose.READ);
		if(preferences.containsKey(name)) {
			return preferences.get(name);
		} else {
			return defaultPreferences.get(name);
		}
	}
	
	@Override
	public String getPreferenceDefault(String name) {
		activate(ActivationPurpose.READ);
		return defaultPreferences.get(name);
	}
	
	@Override
	public void setPreferenceValue(String name, String value) {
		activate(ActivationPurpose.READ);
		final String old = preferences.get(name);
		preferences.put(name, value);
		eventBus.post(new PreferenceChangedEvent(this, name, old, value));
	}

	@Override
	public void setPreferenceDefaultValue(String name, String value) {
		activate(ActivationPurpose.READ);
		defaultPreferences.put(name, value);
	}
	
	@Override
	public void setPreferenceValue(String name, boolean value) {
		setPreferenceValue(name, value ? "true" : "false");
	}
	
	@Override
	public void setPreferenceDefault(String name, boolean value) {
		setPreferenceDefaultValue(name, value ? "true" : "false");
	}
	
	@Override
	public void setPreferenceValue(String name, int value) {
		setPreferenceValue(name, Integer.toString(value));
	}
	
	@Override
	public void setPreferenceDefault(String name, int value) {
		setPreferenceDefaultValue(name, Integer.toString(value));
	}
	
	@Override
	public int getInteger(String name) {
		return getIntegerValue(getPreference(name));
	}
	
	@Override
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
	
	@Override
	public boolean getBoolean(String name) {
		return getBooleanValue(getPreference(name));
	}
	
	@Override
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

	
	@Override
	public void activate(ActivationPurpose activationPurpose) {
		if(activator != null) {
			activator.activate(activationPurpose);
		}
	}

	@Override
	public void bind(Activator activator) {
		if(this.activator == activator) {
			return;
		}
		if(activator != null  && this.activator != null) {
			throw new IllegalStateException("Object can only be bound one to an activator");
		}
		this.activator = activator;
	}
}
