package com.subgraph.sgmail.ui.actions;

import org.eclipse.jface.action.Action;

public class NewIdentityAction extends Action {
	
	
	public NewIdentityAction() {
		super("New Identity");
	}


	public void run() {
        /*
		final IMAPAccount account = findAccount();
		if(account == null) {
			System.out.println("no imap account found");
		}
				
		final Shell shell = Display.getCurrent().getActiveShell();
		final WizardDialog dialog = new WizardDialog(shell, new NewIdentityWizard(model, account));
		dialog.open();
		*/
	}

    /*
	private IMAPAccount findAccount() {
		for(Account account: model.getAccountList().getAccounts()) {
			if(account instanceof IMAPAccount) {
				return (IMAPAccount) account;
			}
		}
		return null;
	}
	*/

}
