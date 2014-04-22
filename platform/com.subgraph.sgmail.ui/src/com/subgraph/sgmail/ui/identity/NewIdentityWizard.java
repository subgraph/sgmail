package com.subgraph.sgmail.ui.identity;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.identity.IdentityManager;

public class NewIdentityWizard extends Wizard {

	//private final ListeningExecutorService executor = createExecutor();
	
	//private final KeyGenerationParameters parameters = new KeyGenerationParameters(); 
	private final IdentityCreationPage page1;
	private final NewKeysPage newKeysPage = new NewKeysPage(null);
	
	
	public NewIdentityWizard(IdentityManager identityManager, MailAccount account) {
		this.page1 = new IdentityCreationPage(identityManager, null, null);
		//parameters.setEmailAddress(account.getEmailAddress());
		//parameters.setRealName(account.getRealname());
	}

	@Override
	public void addPages() {
		addPage(page1);
		addPage(newKeysPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage currentPage) {
		if(currentPage == page1) {
			return newKeysPage;
		}
		return null;
	}

	public boolean canFinish() {
		return true;
	}
	
	@Override
	public boolean performFinish() {
		//ListenableFuture<KeyGenerationResult> future = executor.submit(new KeyGenerationTask(parameters));
		//Futures.addCallback(future, createCallback());
		return true;
	}
	
	/*
	private FutureCallback<KeyGenerationResult> createCallback() {
		return new FutureCallback<KeyGenerationResult>() {

			@Override
			public void onFailure(Throwable t) {
				System.out.println("fail :("+ t);
				t.printStackTrace();
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onSuccess(KeyGenerationResult result) {
				System.out.println("Success! "+ result);
				
			}
		};
	}
	*/

}
