package com.subgraph.sgmail.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.subgraph.sgmail.ui.identity.NewIdentityWizard;

public class NewIdentityAction extends Action {
	
	public NewIdentityAction() {
		super("New Identity");
	}
	
	public void run() {
		final Shell shell = Display.getCurrent().getActiveShell();
		final WizardDialog dialog = new WizardDialog(shell, new NewIdentityWizard("foo@baz.com"));
		dialog.open();
	}

}
