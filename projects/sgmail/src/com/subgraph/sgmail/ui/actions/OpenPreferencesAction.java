package com.subgraph.sgmail.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.ui.preferences.PreferenceDialogFactory;

public class OpenPreferencesAction extends Action {
	private final Model model;
	
	public OpenPreferencesAction(Model model) {
		super("Preferences");
		this.model = model;
	}
	
	@Override
	public void run() {
		final Shell shell = Display.getCurrent().getActiveShell();
		PreferenceDialogFactory factory = new PreferenceDialogFactory();
		PreferenceDialog dialog = factory.createPreferenceDialog(shell, model);
		dialog.open();
		model.commit();
	}

}
