package com.subgraph.sgmail.ui.dialogs;

import com.subgraph.sgmail.identity.OpenPGPException;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.ui.identity.PublicIdentityPane;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class PassphraseDialog extends TitleAreaDialog {



    private final PrivateIdentity identity;
    private Text passphraseText;

    public PassphraseDialog(Shell parentShell, PrivateIdentity identity) {
        super(parentShell);
        this.identity = identity;
    }

    protected Control createContents(Composite parent) {
        final Control contents = super.createContents(parent);
        setMessage("Enter passphrase to unlock private key");
        setTitle("Passphrase");
        enableOkButton(false);
        return contents;

    }

    private void enableOkButton(boolean enabled) {
        final Button button = getButton(IDialogConstants.OK_ID);
        if(button != null) {
            button.setEnabled(enabled);
        }
    }

    protected Control createDialogArea(Composite parent) {
        final Composite composite = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(2, false));

        final PublicIdentityPane publicIdentityPane = new PublicIdentityPane(composite, null, false);
        publicIdentityPane.displayIdentity(identity.getPublicIdentity(), identity);
        publicIdentityPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

        final Label label = new Label(composite, SWT.NONE);
        label.setText("Enter Passphrase:");
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));

        passphraseText = new Text(composite, SWT.PASSWORD | SWT.SINGLE | SWT.BORDER);
        passphraseText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        passphraseText.addModifyListener(createModifyListener());

        return composite;
    }

    private ModifyListener createModifyListener() {
        return new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent modifyEvent) {
                enableOkButton(!passphraseText.getText().isEmpty());
            }
        };
    }

    protected void okPressed() {

        final String passphrase = passphraseText.getText();

        if(identity.isValidPassphrase(passphrase)) {
            try {
                identity.setPassphrase(passphrase);
                setReturnCode(OK);
                close();
                return;
            } catch (OpenPGPException e) {
                e.printStackTrace();
                return;
            }
        }

        setErrorMessage("Invalid passphrase");

    }
}
