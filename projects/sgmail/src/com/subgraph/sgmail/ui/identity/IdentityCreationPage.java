package com.subgraph.sgmail.ui.identity;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.subgraph.sgmail.identity.*;
import com.subgraph.sgmail.model.Identity;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.StoredPrivateIdentity;
import com.subgraph.sgmail.model.StoredPublicIdentity;
import com.subgraph.sgmail.ui.dialogs.AccountDetailsPage;
import com.subgraph.sgmail.ui.dialogs.IdentityPublicationPage;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import java.io.IOException;

public class IdentityCreationPage extends WizardPage {

	private final Model model;
    private final AccountDetailsPage accountDetailsPage;
    private final IdentityPublicationPage publicationPage;

	
	private Button newKeysOption;
	private Button loadKeysOption;
	private Button keyringOption;
	private Button notInterestedOption;
	
	private Label loadKeysLabel;
	private Text loadKeysFilename;
	private Button loadKeysBrowse;

    private Identity generatedIdentity = null;

    public IdentityCreationPage(Model model, AccountDetailsPage accountDetails, IdentityPublicationPage publicationPage) {
		super("identity");
		this.model = model;
        this.accountDetailsPage = accountDetails;
        this.publicationPage = publicationPage;
		setTitle("Create an Identity");
		setDescription("Generate OpenPGP keys for this account or import an existing set of keys from a file or from the local GnuPG keyring");
	}

	public boolean skipIdentityCreation() {
		return notInterestedOption.getSelection();
	}

	@Override
	public void createControl(Composite parent) {
		final SelectionListener listener = createSelectionListener();
		final Composite c = createRootComposite(parent);
		
		createSpacer(c, 20);
		createNewKeysOption(c, listener);
		createSpacer(c, 20);
		createLoadKeysOption(c, listener);
		createSpacer(c, 20);
		createKeyringOption(c, listener);
		createSpacer(c, 20);
		createNotInterestedOption(c, listener);
		
		newKeysOption.setSelection(true);
		onNewKeysSelected();

		setControl(c);
		
	}


	private Composite createRootComposite(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = 20;
		composite.setLayout(layout);
		return composite;
	}

	private SelectionListener createSelectionListener() {
		return new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if(newKeysOption.getSelection()) {
					onNewKeysSelected();
				} else if(loadKeysOption.getSelection()) {
					onLoadKeysSelected();
				} else if(keyringOption.getSelection()) {
					onKeyringSelected();
				} else if(notInterestedOption.getSelection()) {
					onNotInterestedSelected();
				}
			}
		};
	}

	private void onNewKeysSelected() {
		setLoadKeysEnabled(false);
		setPageComplete(true);
	}
	
	private void onLoadKeysSelected() {
		setLoadKeysEnabled(true);
		setPageComplete(false);
	}
	
	private void onKeyringSelected() {
		setLoadKeysEnabled(false);
		setPageComplete(false);
	}
	
	private void onNotInterestedSelected() {
		setLoadKeysEnabled(false);
		setPageComplete(true);
	}
	
	private void setLoadKeysEnabled(boolean enabled) {
		loadKeysLabel.setEnabled(enabled);
		loadKeysFilename.setEnabled(enabled);
		loadKeysBrowse.setEnabled(enabled);
	}
	
	private void createNewKeysOption(Composite parent, SelectionListener listener) {
		newKeysOption = createRadio(parent, "Create a new set of keys");
		newKeysOption.addSelectionListener(listener);
	}

	private void createLoadKeysOption(Composite parent, SelectionListener listener) {
		loadKeysOption =  createRadio(parent, "Import a GPG key or identity from file");
		
		createSpacer(parent, 5);
		
		loadKeysLabel = new Label(parent, SWT.NONE);
		loadKeysLabel.setText("Keyfile:");
		loadKeysLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		loadKeysFilename = new Text(parent, SWT.SINGLE | SWT.BORDER);
		loadKeysFilename.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		loadKeysBrowse = new Button(parent, SWT.PUSH);
		loadKeysBrowse.setText("Browse...");
		loadKeysBrowse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
	}

	private void createKeyringOption(Composite parent, SelectionListener listener) {
		keyringOption = createRadio(parent, "Choose existing key from local GPG keyring");
		keyringOption.addSelectionListener(listener);
		for(PrivateIdentity id : model.getLocalPrivateIdentities()) {
			//System.out.println("id: "+ id);
		}
	}
	
	
	private void createNotInterestedOption(Composite parent, SelectionListener listener) {
		notInterestedOption = createRadio(parent, "I'll do this later.");
		notInterestedOption.addSelectionListener(listener);
	}

	private Button createRadio(Composite parent, String text) {
		final Button button = new Button(parent, SWT.RADIO);
		button.setText(text);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		button.setLayoutData(gd);
		return button;
	}
	
	private void createSpacer(Composite parent, int height) {
		final Label spacer = new Label(parent, SWT.NONE);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		gd.heightHint = height;
		spacer.setLayoutData(gd);
	}

    public Identity getIdentity() {
        return generatedIdentity;
    }

    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    public IWizardPage getNextPage() {
        if(newKeysOption.getSelection()) {
            runKeyGeneration();
            setPageComplete(false);
            return null;
        }
        return super.getNextPage();
    }

    private void runKeyGeneration() {
        System.out.println("runKeyGeneration()");
        final KeyGenerationParameters keyGenerationParameters = new KeyGenerationParameters();
        keyGenerationParameters.setEmailAddress(accountDetailsPage.getUsername() + "@" + accountDetailsPage.getDomain());
        keyGenerationParameters.setRealName(accountDetailsPage.getRealname());

        final KeyGenerationTask task = new KeyGenerationTask(keyGenerationParameters);
        ListenableFuture<KeyGenerationResult> future = model.submitTask(task);
        Futures.addCallback(future, new FutureCallback<KeyGenerationResult>() {
            @Override
            public void onSuccess(KeyGenerationResult keyGenerationResult) {
                if(!keyGenerationResult.isErrorResult()) {
                    onKeyGenerationSucceeded(keyGenerationResult);
                } else {
                    System.out.println("error result: "+ keyGenerationResult.getErrorMessage());
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();;

            }
        });
    }

    private void onKeyGenerationSucceeded(KeyGenerationResult result) {
        try {
            generatedIdentity = createIdentityFromGenerationResult(result);

        } catch (IOException e) {
            e.printStackTrace();
        }
        getControl().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                publicationPage.setIdentity(generatedIdentity);
                IWizardPage nextPage = getWizard().getNextPage(IdentityCreationPage.this);
                setPageComplete(true);
                if(nextPage != null) {
                    getContainer().showPage(nextPage);
                }
            }
        });
    }

    private Identity createIdentityFromGenerationResult(KeyGenerationResult result) throws IOException {
        StoredPublicIdentity pub = new StoredPublicIdentity(result.getPublicKeyRing().getEncoded(), PublicIdentity.KEY_SOURCE_USER_GENERATED);
        StoredPrivateIdentity priv = new StoredPrivateIdentity(result.getSecretKeyRing().getEncoded(), "", pub);
        return new Identity(priv, pub);
    }
}
