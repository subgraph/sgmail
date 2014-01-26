package com.subgraph.sgmail.ui.compose;

import javax.mail.Message;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.subgraph.sgmail.model.Model;

public class ComposeWindow extends Window {

	private MessageComposer composer;
	
	private final Model model;
	private final Message message;
	private final boolean isReplyAll;
	
	public ComposeWindow(Shell parentShell, Model model, Message replyMessage, boolean isReplyAll) {
		super(parentShell);
		this.model = model;
		this.message = replyMessage;
		this.isReplyAll = isReplyAll;
	}
	
	public ComposeWindow(Shell parentShell, Model model) {
		this(parentShell, model, null, false);
	}

	protected Point getInitialSize() {
		final Point p = model.getStoredUserInterfaceState().getShellSize("compose");
		if(p.x != -1 && p.y != -1) {
			return p;
		}
		final Point defaultSize = calculateDefaultInitialSize();
		model.getStoredUserInterfaceState().setShellSize("compose", defaultSize.x, defaultSize.y);
		return defaultSize;
	}
	
	private Point calculateDefaultInitialSize() {
		final Rectangle displayBounds = getParentShell().getDisplay().getBounds();
		final int width = displayBounds.width / 2;
		final int height = displayBounds.height * 75 / 100;
		return new Point(width, height);
	}

	protected Control createContents(Composite parent) {
		composer = new MessageComposer(parent, model, message, isReplyAll, createCloseListener());
		composer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return composer;
	}
	
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText("Compose");
		shell.addControlListener(createControlListener());
	}
	
	private ControlListener createControlListener() {
		return new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				final Rectangle bounds = getShell().getBounds();
				model.getStoredUserInterfaceState().setShellSize("compose", bounds.width, bounds.height);
			}
		};
	}

	private ComposeCloseListener createCloseListener() {
		return new ComposeCloseListener() {
			@Override
			public void closeEvent() {
				close();
			}
		};
	}
}
