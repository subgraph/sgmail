package com.subgraph.sgmail.ui.identity;

import java.util.concurrent.Executors;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.subgraph.sgmail.identity.KeyGenerationParameters;
import com.subgraph.sgmail.identity.KeyGenerationResult;
import com.subgraph.sgmail.identity.KeyGenerationTask;

public class NewIdentityWizard extends Wizard {

	private final ListeningExecutorService executor = createExecutor();
	
	private final KeyGenerationParameters parameters = new KeyGenerationParameters(); 
	
	private final FirstPage page1 = new FirstPage();
	private final NewKeysPage newKeysPage = new NewKeysPage(parameters);
	
	
	
	private static ListeningExecutorService createExecutor() {
		return MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
	}
	
	public NewIdentityWizard(String email) {
		parameters.setEmailAddress(email);
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
