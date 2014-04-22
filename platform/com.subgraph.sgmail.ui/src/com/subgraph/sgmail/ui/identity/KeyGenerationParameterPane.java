package com.subgraph.sgmail.ui.identity;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.subgraph.sgmail.identity.KeyGenerationParameters;

public class KeyGenerationParameterPane extends Composite {

	private final KeyGenerationParameters parameters;
	private final Label emailLabel;
	private final Label signingKey;
	private final Label encryptionKey;
	private final Label expiryLabel;
	
	KeyGenerationParameterPane(Composite parent, KeyGenerationParameters parameters) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(2, false));
		this.parameters = parameters;
		emailLabel = createLabel("Email:");
		
		signingKey = createLabel("Signing key: ");
		encryptionKey = createLabel("Encryption key: ");
		expiryLabel = createLabel("Expiry:");
		refresh();
	}
	
	private Label createLabel(String labelLabel) {
		final Label l = new Label(this, SWT.NONE);
		l.setText(labelLabel);
		l.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		
		final Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		return label;
	}
	
	public void refresh() {
		emailLabel.setText(parameters.getEmailAddress());
		signingKey.setText(getSigningKeyText());
		encryptionKey.setText(getEncryptionKeyText());
		expiryLabel.setText("No expiry");
	}
	
	private String getEncryptionKeyText() {
		final String bits = "("+ getKeyLengthText(parameters.getEncryptionKeyLength()) + " bits)";
		switch(parameters.getKeyType()) {
		case RSA_AND_RSA:
			return "RSA "+ bits;
		case RSA_SIGN_ONLY:
		case DSA_SIGN_ONLY:
			return "None";
		case DSA_AND_ELGAMAL:
			return "ElGamal "+ bits;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	private String getSigningKeyText() {
		String bits = "("+ getKeyLengthText(parameters.getSigningKeyLength()) + " bits)";
		switch(parameters.getKeyType()) {
		case RSA_AND_RSA:
		case RSA_SIGN_ONLY:
			return "RSA "+ bits;
		case DSA_AND_ELGAMAL:
		case DSA_SIGN_ONLY:
			return "DSA "+ bits;
		default:
			throw new IllegalArgumentException();
		}
	}
	
	private String getKeyLengthText(KeyGenerationParameters.KeyLength length) {
		switch(length) {
		case KEY_1024:
			return "1024";
		case KEY_2048:
			return "2048";
		case KEY_4096:
			return "4096";
		case KEY_8192:
			return "8192";
		default:
			throw new IllegalArgumentException();
		}
	}
}
