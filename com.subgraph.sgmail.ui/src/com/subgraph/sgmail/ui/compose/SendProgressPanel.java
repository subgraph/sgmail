package com.subgraph.sgmail.ui.compose;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ProgressBar;

public class SendProgressPanel extends Composite {

	private final Label progressLabel;
	
	SendProgressPanel(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(createLayout());
		createProgressBar();
		progressLabel = createProgressLabel();
	}

	public void setProgressText(String message) {
		progressLabel.setText(message);
	}

	private Layout createLayout() {
		final GridLayout layout = new GridLayout(2, false);
		return layout;
	}
	
	
	private ProgressBar createProgressBar() {
		final ProgressBar bar = new ProgressBar(this, SWT.INDETERMINATE);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		bar.setLayoutData(gd);
		return bar;
	}

	private Label createProgressLabel() {
		final Label label = new Label(this, SWT.NONE);
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		label.setLayoutData(gd);
		return label;
	}
}
