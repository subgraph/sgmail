package com.subgraph.sgmail.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.ui.ImageCache;
import com.subgraph.sgmail.ui.compose.ComposeWindow;

public class ComposeMessageAction extends Action {
	private final Model model;
	
	public ComposeMessageAction(Model model) {
		super("Compose", createImageDescriptor());
		this.model = model;
	}

	private static ImageDescriptor createImageDescriptor() {
		final Image image = ImageCache.getInstance().getImage(ImageCache.COMPOSE_IMAGE);
		return ImageDescriptor.createFromImage(image);
	}
	
	public void run() {
		ComposeWindow compose = new ComposeWindow(Display.getDefault().getActiveShell(), model);
		compose.open();

	}
}
