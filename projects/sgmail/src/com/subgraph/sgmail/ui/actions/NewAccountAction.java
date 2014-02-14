package com.subgraph.sgmail.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.subgraph.sgmail.model.GmailIMAPAccount;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.SMTPAccount;
import com.subgraph.sgmail.servers.ServerInformation;
import com.subgraph.sgmail.ui.dialogs.NewAccountDialog;
import com.subgraph.sgmail.ui.dialogs.NewAccountWizard;

public class NewAccountAction extends Action {
	
	private final Model model;
	
	public NewAccountAction(Model model) {
		super("New Account");
		this.model = model;
	}
	
	public void run() {
		final Shell shell = Display.getCurrent().getActiveShell();
		final WizardDialog dialog = new WizardDialog(shell, new NewAccountWizard(model));
		dialog.open();
	}
}
