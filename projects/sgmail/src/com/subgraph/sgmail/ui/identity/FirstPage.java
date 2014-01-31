package com.subgraph.sgmail.ui.identity;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FirstPage extends WizardPage {

	FirstPage() {
		super("New Identity");
		setTitle("Title...");
		setDescription("description goes here");
	}

	@Override
	public void createControl(Composite parent) {
		final Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(3, false));;
		
		Button b1 = createRadio(c, "Create a new set of keys");
		b1.setSelection(true);
		
		
		final Button b2 = createRadio(c, "Import a GPG key or identity from file");
		
		
		
		Label l = new Label(c, SWT.NONE);
		l.setText("Keyfile:");
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		
		final Text t = new Text(c, SWT.SINGLE | SWT.BORDER);
		t.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		t.setEnabled(false);
		
		final Button browse = new Button(c, SWT.PUSH);
		browse.setText("Browse...");
		browse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		browse.setEnabled(false);
		
		
		b2.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				boolean val = b2.getSelection();
				t.setEnabled(val);
				browse.setEnabled(val);
			}
		});
		Button b3 = createRadio(c, "Choose existing key from local gpg key ring");
		b3.setEnabled(false);
		setControl(c);
		
	}

	private Button createRadio(Composite parent, String text) {
		final Button button = new Button(parent, SWT.RADIO);
		button.setText(text);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		button.setLayoutData(gd);
		return button;
	}
}
