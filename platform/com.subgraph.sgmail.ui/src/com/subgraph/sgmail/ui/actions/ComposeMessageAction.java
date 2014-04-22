package com.subgraph.sgmail.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.identity.IdentityManager;
import com.subgraph.sgmail.openpgp.MessageProcessor;
import com.subgraph.sgmail.ui.Activator;
import com.subgraph.sgmail.ui.ImageCache;
import com.subgraph.sgmail.ui.compose.ComposeWindow;

public class ComposeMessageAction extends Action {
	
	public ComposeMessageAction() {
		super("Compose", createImageDescriptor());
	}

	private static ImageDescriptor createImageDescriptor() {
		final Image image = ImageCache.getInstance().getImage(ImageCache.COMPOSE_IMAGE);
		return ImageDescriptor.createFromImage(image);
	}
	
	public void run() {
		final Activator activator = Activator.getInstance();
		final Model model = activator.getModel();
		
        if(model.getAccountList().getAccounts().isEmpty()) {
            return;
        }
		final IEventBus eventBus = activator.getEventBus();
		final MessageProcessor messageProcessor = activator.getMessageProcessor();
		final IdentityManager identityManager = activator.getIdentityManager();
		final JavamailUtils javamailUtils = activator.getJavamailUtils();
		
	
		ComposeWindow compose = new ComposeWindow(Display.getDefault().getActiveShell(), javamailUtils, eventBus, messageProcessor, identityManager, model);
		compose.open();

	}
}
