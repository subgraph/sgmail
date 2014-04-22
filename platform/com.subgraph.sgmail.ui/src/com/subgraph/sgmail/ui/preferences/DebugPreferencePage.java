package com.subgraph.sgmail.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import com.subgraph.sgmail.database.Preferences;

public class DebugPreferencePage extends FieldEditorPreferencePage {


	public DebugPreferencePage() {
		super(GRID);
		setTitle("Debug");
	}

	@Override
	protected void createFieldEditors() {
		addBoolean(Preferences.IMAP_DEBUG_OUTPUT, "Enable IMAP debug");
		addBoolean(Preferences.DUMP_SELECTED_MESSAGE, "Dump selected messages to stdout");
	}
	
	private void addBoolean(String preferenceName, String label) {
		final BooleanFieldEditor editor = new BooleanFieldEditor(preferenceName, label, getFieldEditorParent());
		addField(editor);
	}

}
