package com.subgraph.sgmail.ui.identity;

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

import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.model.Model;

public class FirstPage extends WizardPage {

	private final Model model;
	
	private Button newKeysOption;
	private Button loadKeysOption;
	private Button keyringOption;
	
	private Label loadKeysLabel;
	private Text loadKeysFilename;
	private Button loadKeysBrowse;
	
	
	FirstPage(Model model) {
		super("first");
		this.model = model;
		setTitle("Create an Identity");
		setDescription("description goes here");
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
		
		newKeysOption.setSelection(true);
		onNewKeysSelected();

		setControl(c);
		
	}


	private Composite createRootComposite(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout(3, false);
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
				}
			}
		};
	}

	private void onNewKeysSelected() {
		setLoadKeysEnabled(false);
	}
	
	private void onLoadKeysSelected() {
		setLoadKeysEnabled(true);
	}
	
	private void onKeyringSelected() {
		setLoadKeysEnabled(false);
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
			System.out.println("id: "+ id);
		}
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
}
