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

import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.database.StoredUserInterfaceState;
import com.subgraph.sgmail.identity.IdentityManager;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.openpgp.MessageProcessor;

public class ComposeWindow extends Window {

	private MessageComposer composer;
	
	private final JavamailUtils javamailUtils;
	private final IEventBus eventBus;
	private final MessageProcessor messageProcessor;
	private final IdentityManager identityManager;
	private final Model model;

	private final StoredMessage message;
	private final boolean isReplyAll;
	
	public ComposeWindow(Shell parentShell, JavamailUtils javamailUtils, IEventBus eventBus, MessageProcessor messageProcessor, IdentityManager identityManager, Model model, StoredMessage replyMessage, boolean isReplyAll) {
		super(parentShell);
		this.javamailUtils = javamailUtils;
		this.eventBus = eventBus;
		this.messageProcessor = messageProcessor;
		this.identityManager = identityManager;
		this.model = model;
		this.message = replyMessage;
		this.isReplyAll = isReplyAll;
	}
	
	public ComposeWindow(Shell parentShell, JavamailUtils javamailUtils, IEventBus eventBus, MessageProcessor messageProcessor, IdentityManager identityManager, Model model) {
		this(parentShell, javamailUtils, eventBus, messageProcessor, identityManager, model, null, false);
	}

	protected Point getInitialSize() {
		final StoredUserInterfaceState uiState = model.getStoredUserInterfaceState();
		final int width = uiState.getShellWidth("compose");
		final int height = uiState.getShellHeight("compose");
		final Point p = new Point(width, height);
		if(p.x != -1 && p.y != -1) {
			return p;
		}
		final Point defaultSize = calculateDefaultInitialSize();
		uiState.setShellSize("compose", defaultSize.x, defaultSize.y);
		return defaultSize;
	}
	
	private Point calculateDefaultInitialSize() {
		final Rectangle displayBounds = getParentShell().getDisplay().getBounds();
		final int width = displayBounds.width / 2;
		final int height = displayBounds.height * 75 / 100;
		return new Point(width, height);
	}

	protected Control createContents(Composite parent) {
		composer = new MessageComposer(parent, javamailUtils, eventBus, messageProcessor, identityManager, model, message, isReplyAll, createCloseListener());
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
				final StoredUserInterfaceState uiState = model.getStoredUserInterfaceState();
				uiState.setShellSize("compose", bounds.width, bounds.height);
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
