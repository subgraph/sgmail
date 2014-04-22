package com.subgraph.sgmail.ui.identity;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.subgraph.sgmail.identity.KeyGenerationParameters;

public class NewKeysPage extends WizardPage {

	private final KeyGenerationParameters parameters;
	
	protected NewKeysPage(KeyGenerationParameters parameters) {
		super("");
		this.parameters = parameters;
	}

	@Override
	public void createControl(Composite parent) {
		
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(2, false));
		
		Label label = new Label(c, SWT.NONE);
		label.setText("Name: ");
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		
		Text text = new Text(c, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		
		Group g = new Group(c, SWT.NONE);
		g.setText("Key generation parameters");
		g.setLayout(new FillLayout());
		g.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		final KeyGenerationParameterPane pane = new KeyGenerationParameterPane(g, parameters);
		setControl(c);
		
	}

}
