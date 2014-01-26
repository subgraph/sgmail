package com.subgraph.sgmail.ui.compose;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

public class ComposerButtons extends Composite {

	
	private final Button sendButton;
	private final SendProgressPanel progressPanel;
	
	ComposerButtons(Composite parent, SelectionListener sendListerner, SelectionListener cancelListener) {
		super(parent, SWT.NONE);
		setLayout(createLayout());
		sendButton = createSendButton(sendListerner);
		createCancelButton(cancelListener);
		progressPanel = createProgressPanel();
		setProgressVisible(false);
	}
	
	public void setProgressVisible(boolean value) {
		progressPanel.setVisible(value);
	}
	
	public void setProgressMessage(String message) {
		progressPanel.setProgressText(message);
	}

	private Layout createLayout() {
		final GridLayout layout = new GridLayout(3, false);
		layout.horizontalSpacing = 0;
		layout.marginBottom = 0;
		layout.marginTop = 0;
		return layout;
	}

	private Button createSendButton(SelectionListener listener) {
		return createButton(" Send ", listener);
	}
	
	private Button createCancelButton(SelectionListener listener) {
		return createButton("Cancel", listener);
	}
	
	private Button createButton(String buttonText, SelectionListener listener) {
		final Button button = new Button(this, SWT.PUSH);
		button.setText(buttonText);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		button.setLayoutData(gd);
		button.addSelectionListener(listener);
		return button;
	}
	
	private SendProgressPanel createProgressPanel() {
		final SendProgressPanel panel = new SendProgressPanel(this);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		panel.setLayoutData(gd);
		return panel;
	}

	public void setSendButtonEnabled(boolean value) {
		sendButton.setEnabled(value);
	}
}
