package com.subgraph.sgmail.ui.panes.right;

import javax.mail.Message;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import com.subgraph.sgmail.ui.MessageBodyUtils;

public class MessageBodyViewer extends Composite {

	public MessageBodyViewer(Composite parent, Message message) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		StyledText text = new StyledText(this, SWT.WRAP | SWT.READ_ONLY);
		text.setMargins(26, 10, 10, 10);
		text.setFont(JFaceResources.getTextFont());
		
		final String body = MessageBodyUtils.getTextBody(message);
		if(body != null) {
			text.setText(body);
		}
	}
}
