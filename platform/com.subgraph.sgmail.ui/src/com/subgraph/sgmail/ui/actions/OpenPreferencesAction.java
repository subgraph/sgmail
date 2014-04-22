package com.subgraph.sgmail.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.ui.Activator;
import com.subgraph.sgmail.ui.preferences.PreferenceDialogFactory;

public class OpenPreferencesAction extends Action {
	
	public OpenPreferencesAction() {
		super("Preferences");
	}
	
	@Override
	public void run() {
		final Shell shell = Display.getCurrent().getActiveShell();
		final Model model = Activator.getInstance().getModel();
		PreferenceDialogFactory factory = new PreferenceDialogFactory();
		
		PreferenceDialog dialog = factory.createPreferenceDialog(shell, model);
		dialog.open();
		model.getDatabase().commit();
	}

}
