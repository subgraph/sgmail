package com.subgraph.sgmail.ui.preferences;

import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.database.Preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;

public class AccountPreferencePage extends FieldEditorPreferencePage {
	
	private final IPreferenceStore store;
	
	AccountPreferencePage(MailAccount account) {
		super(GRID);
		setTitle(account.getEmailAddress());
		this.store = new PreferenceStoreAdapter(account.getPreferences());
	}
	
	@Override
	protected void createFieldEditors() {
		addBoolean(Preferences.ACCOUNT_DEFAULT_SIGN, "Sign messages by default");
		addBoolean(Preferences.ACCOUNT_DEFAULT_ENCRYPT, "Encrypt messages by default");
	}
	
	private void addBoolean(String name, String label) {
		final BooleanFieldEditor fieldEditor = new BooleanFieldEditor(name, label, getFieldEditorParent());
		addField(fieldEditor);
	}

	protected IPreferenceStore doGetPreferenceStore() {
		return store;
    }

}
