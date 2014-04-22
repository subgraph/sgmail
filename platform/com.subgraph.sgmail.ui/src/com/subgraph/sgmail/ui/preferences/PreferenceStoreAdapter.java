package com.subgraph.sgmail.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;

import com.subgraph.sgmail.database.Preferences;

public class PreferenceStoreAdapter implements IPreferenceStore {

	private final Preferences storedPreferences;
	
	PreferenceStoreAdapter(Preferences storedPreferences) {
		this.storedPreferences = storedPreferences;
	}
	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(String name) {
		return storedPreferences.contains(name);
	}

	@Override
	public void firePropertyChangeEvent(String name, Object oldValue,
			Object newValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean getBoolean(String name) {
		return getBooleanValue(storedPreferences.getPreference(name));
	}

	@Override
	public boolean getDefaultBoolean(String name) {
		return getBooleanValue(storedPreferences.getPreferenceDefault(name));
	}

	private static boolean getBooleanValue(String value) {
		if(value == null) {
			return BOOLEAN_DEFAULT_DEFAULT;
		} else {
			return value.equals(TRUE);
		}
	}
	@Override
	public double getDefaultDouble(String name) {
		return getDoubleValue(storedPreferences.getPreferenceDefault(name));
	}

	@Override
	public float getDefaultFloat(String name) {
		return getFloatValue(storedPreferences.getPreferenceDefault(name));
	}

	private static double getDoubleValue(String value) {
		if(value == null) {
			return DOUBLE_DEFAULT_DEFAULT;
		} else {
			try {
				return new Double(value).doubleValue();
			} catch (NumberFormatException e) {
				return DOUBLE_DEFAULT_DEFAULT;
			}
		}
	}
	
	private static float getFloatValue(String value) {
		if(value == null) {
			return FLOAT_DEFAULT_DEFAULT;
		} else {
			try {
				return new Float(value).floatValue();
			} catch (NumberFormatException e) {
				return FLOAT_DEFAULT_DEFAULT;
			}
		}
	}
	
	private static int getIntValue(String value) {
		if(value == null) {
			return INT_DEFAULT_DEFAULT;
		} else {
			try {
				return new Integer(value).intValue();
			} catch (NumberFormatException e) {
				return INT_DEFAULT_DEFAULT;
			}
		}
	}
	
	private static long getLongValue(String value) {
		if(value == null) {
			return LONG_DEFAULT_DEFAULT;
		} else {
			try {
				return new Long(value).longValue();
			} catch (NumberFormatException e) {
				return LONG_DEFAULT_DEFAULT;
			}
		}
	}
	
	@Override
	public int getDefaultInt(String name) {
		return getIntValue(storedPreferences.getPreferenceDefault(name));
	}

	@Override
	public long getDefaultLong(String name) {
		return getLongValue(storedPreferences.getPreferenceDefault(name));
	}

	@Override
	public String getDefaultString(String name) {
		return storedPreferences.getPreferenceDefault(name);
	}

	@Override
	public double getDouble(String name) {
		return getDoubleValue(storedPreferences.getPreference(name));
	}

	@Override
	public float getFloat(String name) {
		return getFloatValue(storedPreferences.getPreference(name));
	}

	@Override
	public int getInt(String name) {
		return getIntValue(storedPreferences.getPreference(name));
	}

	@Override
	public long getLong(String name) {
		return getLongValue(storedPreferences.getPreference(name));
	}

	@Override
	public String getString(String name) {
		return storedPreferences.getPreference(name);
	}

	@Override
	public boolean isDefault(String name) {
		String defaultValue = storedPreferences.getPreferenceDefault(name);
		if(defaultValue == null) {
			return false;
		} else {
			return defaultValue.equals(storedPreferences.getPreference(name));
		}
	}

	@Override
	public boolean needsSaving() {
		return false;
	}

	@Override
	public void putValue(String name, String value) {
		storedPreferences.setPreferenceValue(name, value);
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefault(String name, double value) {
		storedPreferences.setPreferenceDefaultValue(name, Double.toString(value));
	}

	@Override
	public void setDefault(String name, float value) {
		storedPreferences.setPreferenceDefaultValue(name, Float.toString(value));
	}

	@Override
	public void setDefault(String name, int value) {
		storedPreferences.setPreferenceDefaultValue(name, Integer.toString(value));
	}

	@Override
	public void setDefault(String name, long value) {
		storedPreferences.setPreferenceDefaultValue(name, Long.toString(value));
		
	}

	@Override
	public void setDefault(String name, String defaultObject) {
		storedPreferences.setPreferenceDefaultValue(name, defaultObject);
	}

	@Override
	public void setDefault(String name, boolean value) {
		storedPreferences.setPreferenceDefaultValue(name, (value) ? TRUE : FALSE);
	}

	@Override
	public void setToDefault(String name) {
		storedPreferences.unsetPreference(name);
	}

	@Override
	public void setValue(String name, double value) {
		storedPreferences.setPreferenceValue(name, Double.toString(value));
	}

	@Override
	public void setValue(String name, float value) {
		storedPreferences.setPreferenceValue(name, Float.toString(value));
	}

	@Override
	public void setValue(String name, int value) {
		storedPreferences.setPreferenceValue(name, Integer.toString(value));
	}

	@Override
	public void setValue(String name, long value) {
		storedPreferences.setPreferenceValue(name, Long.toString(value));
	}

	@Override
	public void setValue(String name, String value) {
		storedPreferences.setPreferenceValue(name, value);
	}

	@Override
	public void setValue(String name, boolean value) {
		storedPreferences.setPreferenceValue(name, (value) ? TRUE : FALSE);
	}

}
