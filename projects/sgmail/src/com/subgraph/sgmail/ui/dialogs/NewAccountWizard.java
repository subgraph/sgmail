package com.subgraph.sgmail.ui.dialogs;

import com.subgraph.sgmail.model.IMAPAccount;
import com.subgraph.sgmail.model.Identity;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.ui.identity.IdentityCreationPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

public class NewAccountWizard extends Wizard {

    private final Model model;
	private final AccountDetailsPage accountDetailsPage;
	private final IdentityCreationPage identityCreationPage;
    private final IdentityPublicationPage identityPublicationPage;
	private final AccountCreationFinishedPage finishedPage;
	
	public NewAccountWizard(Model model) {
        this.model = model;
		this.accountDetailsPage = new AccountDetailsPage(model);
        this.identityPublicationPage = new IdentityPublicationPage(model, accountDetailsPage);
		this.identityCreationPage = new IdentityCreationPage(model, accountDetailsPage, identityPublicationPage);
		this.finishedPage = new AccountCreationFinishedPage();
		//setNeedsProgressMonitor(true);
	}
	
	@Override
	public void addPages() {
		addPage(accountDetailsPage);
		addPage(identityCreationPage);
        addPage(identityPublicationPage);
		addPage(finishedPage);
	}
	
	@Override
	public
	IWizardPage getNextPage(IWizardPage currentPage) {
		if(currentPage == accountDetailsPage) {
			return identityCreationPage;
		} else if(currentPage == identityCreationPage) {
			if(identityCreationPage.skipIdentityCreation()) {
				return finishedPage;
			} else {
                return identityPublicationPage;
			}
		} else if(currentPage == identityPublicationPage) {
            return finishedPage;
        }
		return null;
	}
	
	public boolean canFinish() {
		return getContainer().getCurrentPage() == finishedPage;
	}

	@Override
	public boolean performFinish() {
        final IMAPAccount imapAccount = accountDetailsPage.createIMAPAccount();
        model.addAccount(imapAccount);
        final Identity identity = identityCreationPage.getIdentity();
        if(identity != null) {
            imapAccount.setIdentity(identity);
        }
        return true;
	}
}
