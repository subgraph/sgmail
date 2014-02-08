package com.subgraph.sgmail.ui.dialogs;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.ui.identity.IdentityCreationPage;

public class NewAccountWizard extends Wizard {

	private final AccountDetailsPage accountDetailsPage = new AccountDetailsPage();
	private final IdentityCreationPage identityCreationPage;
	
	public NewAccountWizard(Model model) {
		this.identityCreationPage = new IdentityCreationPage(model);
		setNeedsProgressMonitor(true);
	}
	
	@Override
	public void addPages() {
		addPage(accountDetailsPage);
		addPage(identityCreationPage);
	}
	
	@Override
	public 
	IWizardPage getNextPage(IWizardPage currentPage) {
		if(currentPage == accountDetailsPage) {
			return identityCreationPage;
		}
		return null;
		
	}
	
	public boolean canFinish() {
		return false;
	}

	@Override
	public boolean performFinish() {
		/*
		 * Extract page fields here
		 */
		// TODO Auto-generated method stub
		return false;
	}

}
