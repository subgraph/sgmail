package com.subgraph.sgmail.ui.compose;

import java.util.Map;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.ImmutableMap;

public class RecipientSection {

	final static int TYPE_TO = 0;
	final static int TYPE_CC = 1;
	final static int TYPE_BCC = 2;
	
	final static Map<Integer, String> labels = 
			ImmutableMap.of(TYPE_TO, "To", TYPE_CC, "CC", TYPE_BCC, "BCC");
	
	private final int type;
	private final Text textField;
	
	RecipientSection(Composite composite, int type, ModifyListener modifyListener) {
		this.type = type;
		createLabel(composite, type);
		textField = createTextField(composite, modifyListener);
	}
	
	public void setText(String text) {
		textField.setText(text);
	}

	public int getType() {
		return type;
	}

	public boolean isValid() {
		final boolean allowEmpty = (type != TYPE_TO);
		return isValid(allowEmpty);
	}
	
	private boolean isValid(boolean allowEmpty) {
		String s = textField.getText().trim();
		if(s.isEmpty()) return allowEmpty;
		
		return isValidRecipient(s);
	}
	
	public InternetAddress[] getAddresses() {
		if(!isValid(false)) {
			return new InternetAddress[0];
		}
		try {
			return InternetAddress.parse(textField.getText().trim());
		} catch (AddressException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new InternetAddress[0];
		}
	}

	private boolean isValidRecipient(String fieldText) {
		try {
			return InternetAddress.parse(fieldText, false).length > 0;
		} catch (AddressException e) {
			return false;
		}
	}
	
	private Label createLabel(Composite composite, int type) {
		final Label label = new Label(composite, SWT.NONE);
		label.setText(getLabelText(type));
		label.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		return label;
	}
	
	private String getLabelText(int type) {
		if(labels.containsKey(type)) {
			return labels.get(type) + ":";
		} else {
			return "??:";
		}
	}
	
	private Text createTextField(Composite composite, ModifyListener modifyListener) {
		final Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		text.setLayoutData(gd);
		text.addModifyListener(modifyListener);
		return text;
	}
}
