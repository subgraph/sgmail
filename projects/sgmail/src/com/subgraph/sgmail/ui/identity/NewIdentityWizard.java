package com.subgraph.sgmail.ui.identity;

import com.google.common.util.concurrent.*;
import com.subgraph.sgmail.identity.KeyGenerationParameters;
import com.subgraph.sgmail.identity.KeyGenerationResult;
import com.subgraph.sgmail.identity.KeyGenerationTask;
import com.subgraph.sgmail.model.IMAPAccount;
import com.subgraph.sgmail.model.Model;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import java.util.concurrent.Executors;

public class NewIdentityWizard extends Wizard {

	private final ListeningExecutorService executor = createExecutor();
	
	private final KeyGenerationParameters parameters = new KeyGenerationParameters(); 
	private final IdentityCreationPage page1;
	private final NewKeysPage newKeysPage = new NewKeysPage(parameters);
	
	
	
	private static ListeningExecutorService createExecutor() {
		return MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
	}
	
	public NewIdentityWizard(Model model, IMAPAccount account) {
		this.page1 = new IdentityCreationPage(model, null, null);
		parameters.setEmailAddress(account.getEmailAddress());
		parameters.setRealName(account.getRealname());
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
		ListenableFuture<KeyGenerationResult> future = executor.submit(new KeyGenerationTask(parameters));
		Futures.addCallback(future, createCallback());
		return true;
	}
	
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

}
