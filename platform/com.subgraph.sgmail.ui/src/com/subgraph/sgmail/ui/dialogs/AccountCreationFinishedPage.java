package com.subgraph.sgmail.ui.dialogs;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class AccountCreationFinishedPage extends WizardPage {

	private Button startSynchronizingButton;
	
	public AccountCreationFinishedPage() {
		super("finished");
        setTitle("Account Creation Finished");
        setDescription("New account has been created");
	}
	
	
	@Override
	public void createControl(Composite parent) {
		final Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout());
		startSynchronizingButton = createStartSynchronizingMailButton(c);
		startSynchronizingButton.setSelection(true);
		setControl(c);
	}
	
	private Button createStartSynchronizingMailButton(Composite parent) {
		Button button = new Button(parent, SWT.CHECK);
		button.setText("Start downloading mail");
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		return button;
	}
	

}
