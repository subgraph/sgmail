package com.subgraph.sgmail.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.subgraph.sgmail.model.Account;
import com.subgraph.sgmail.model.IMAPAccount;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.ui.identity.NewIdentityWizard;

public class NewIdentityAction extends Action {
	
	private final Model model;
	
	public NewIdentityAction(Model model) {
		super("New Identity");
		this.model = model;
	}
	
	public void run() {
		final IMAPAccount account = findAccount();
		if(account == null) {
			System.out.println("no imap account found");
		}
				
		final Shell shell = Display.getCurrent().getActiveShell();
		final WizardDialog dialog = new WizardDialog(shell, new NewIdentityWizard(model, account));
		dialog.open();
	}
	
	private IMAPAccount findAccount() {
		for(Account account: model.getAccounts()) {
			if(account instanceof IMAPAccount) {
				return (IMAPAccount) account;
			}
		}
		return null;
	}

}
