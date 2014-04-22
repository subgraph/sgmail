package com.subgraph.sgmail.ui.dialogs;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.subgraph.sgmail.accounts.AccountList;
import com.subgraph.sgmail.database.Database;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.identity.IdentityManager;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.imap.IMAPAccount;
import com.subgraph.sgmail.imap.IMAPAccountList;
import com.subgraph.sgmail.imap.IMAPFactory;
import com.subgraph.sgmail.ui.identity.IdentityCreationPage;

public class NewAccountWizard extends Wizard {

	private final Model model;
	private final IMAPFactory imapFactory;
	
	private final AccountDetailsPage accountDetailsPage;
	private final IdentityCreationPage identityCreationPage;
    private final IdentityPublicationPage identityPublicationPage;
	private final AccountCreationFinishedPage finishedPage;
	
	public NewAccountWizard(Model model, IMAPFactory imapFactory, IdentityManager identityManager) {
		this.model = model;
		this.imapFactory = imapFactory;
		this.accountDetailsPage = new AccountDetailsPage(model);
        this.identityPublicationPage = new IdentityPublicationPage(model, accountDetailsPage);
		this.identityCreationPage = new IdentityCreationPage(identityManager, accountDetailsPage, identityPublicationPage);
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
        final AccountList accountList = model.getAccountList();
        accountList.addAccount(imapAccount.getMailAccount());
        final IMAPAccountList imapAccountList = getIMAPAccountList();
        imapAccountList.addAccount(imapAccount);
        final PrivateIdentity identity = identityCreationPage.getIdentity();
        if(identity != null) {
            imapAccount.getMailAccount().setIdentity(identity);
        }
        return true;
	}

    private IMAPAccountList getIMAPAccountList() {
    	final Database database = model.getDatabase();
        final IMAPAccountList imapAccountList = database.getSingleton(IMAPAccountList.class);
        if(imapAccountList != null) {
            return imapAccountList;
        }
        final IMAPAccountList newList = imapFactory.createIMAPAccountList();
        database.store(newList);
        database.commit();
        return newList;
    }
}
