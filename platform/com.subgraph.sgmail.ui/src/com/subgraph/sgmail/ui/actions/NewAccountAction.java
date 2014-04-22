package com.subgraph.sgmail.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.identity.IdentityManager;
import com.subgraph.sgmail.imap.IMAPFactory;
import com.subgraph.sgmail.ui.Activator;
import com.subgraph.sgmail.ui.dialogs.NewAccountWizard;

public class NewAccountAction extends Action {
	
	
	public NewAccountAction() {
		super("New Account");
	}
	
	public void run() {
		final Shell shell = Display.getCurrent().getActiveShell();
		final Activator activator = Activator.getInstance();
		final Model model = activator.getModel();
		final IMAPFactory imapFactory = activator.getIMAPFactory();
		final IdentityManager identityManager = activator.getIdentityManager();
		final WizardDialog dialog = new WizardDialog(shell, new NewAccountWizard(model, imapFactory, identityManager));
		dialog.open();
	}
}
