package com.subgraph.sgmail.ui.dialogs;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.ui.Resources;
import com.subgraph.sgmail.ui.identity.PublicIdentityPane;

public class IdentityPublicationPage extends WizardPage {

	private final Model model;
    private final AccountDetailsPage accountDetailsPage;
    private PrivateIdentity identity;
    private PublicIdentityPane publicIdentityPane;
    private Label errorLabel;
    private Button publishButton;
    private Button registerButton;

	public IdentityPublicationPage(Model model, AccountDetailsPage accountDetailsPage) {
        super("publish");
        setTitle("Identity Publication");
        setDescription("...");
        this.model = model;
        this.accountDetailsPage = accountDetailsPage;
	}

    public void setIdentity(PrivateIdentity identity) {
        this.identity = identity;
        publicIdentityPane.displayIdentity(identity.getPublicIdentity(), identity);

    }

	@Override
	public void createControl(Composite parent) {
		final Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout());

        publicIdentityPane = new PublicIdentityPane(c, model, true);
        publicIdentityPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        errorLabel = createErrorLabel(c);
        publishButton = createPublishButton(c);
        registerButton = createRegisterButton(c);
        setControl(c);
	}

    private Label createErrorLabel(Composite parent) {
        final Label label = new Label(parent, SWT.NONE);
        label.setForeground(JFaceResources.getColorRegistry().get(Resources.COLOR_ERROR_MESSAGE));
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        return label;
    }

    private Button createPublishButton(Composite parent) {
        return createCheckButton(parent, "Publish this identity on identity server");
    }

    private Button createRegisterButton(Composite parent) {
        return createCheckButton(parent, "Register this identity for this email address");
    }

    private Button createCheckButton(Composite parent, String text) {
        final Button button = new Button(parent, SWT.CHECK);
        button.setText(text);
        final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        button.setLayoutData(gd);
        button.setSelection(true);
        return button;
    }


    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    public IWizardPage getNextPage() {
        if(registerButton.getSelection()) {
            registerIdentity();
            setPageComplete(false);
            return null;
        }
        return super.getNextPage();
    }

    private void registerIdentity() {
        final String imapServer = accountDetailsPage.getIncomingServer().getHostname();
        final String imapLogin = accountDetailsPage.getIncomingLogin();
        final String imapPassword = accountDetailsPage.getPassword();
        final String emailAddress = accountDetailsPage.getUsername() + "@" + accountDetailsPage.getDomain();
        /*
        KeyRegistrationTask task = new KeyRegistrationTask(model, emailAddress, identity.getPublicIdentity(), imapServer, imapLogin, imapPassword);
        final ListenableFuture<KeyRegistrationResult> future = model.submitTask(task);
        Futures.addCallback(future, createCallback());
        */
    }
/*
    private FutureCallback<KeyRegistrationResult> createCallback() {
        return new FutureCallback<KeyRegistrationResult>() {
            @Override
            public void onSuccess(KeyRegistrationResult keyRegistrationResult) {
                if(!keyRegistrationResult.isError()) {
                    onKeyRegistrationSucceeded();
                } else {
                    asyncDisplayError(keyRegistrationResult.getErrorMessage());
                }
            }

            @Override
            public void onFailure(final Throwable throwable) {
                throwable.printStackTrace();
                asyncDisplayError(throwable.getMessage());
            }
        };
    }
    */

    private void asyncDisplayError(final String message) {
        if(errorLabel == null || errorLabel.isDisposed()) {
            return;
        }
        errorLabel.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                errorLabel.setText(message);
            }
        });
    }

    private void onKeyRegistrationSucceeded() {
        getControl().getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                IWizardPage nextPage = getWizard().getNextPage(IdentityPublicationPage.this);
                setPageComplete(true);
                if(nextPage != null) {
                    getContainer().showPage(nextPage);
                }
            }
        });
    }
}
