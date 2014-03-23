package com.subgraph.sgmail.ui.panes.right;

import com.subgraph.sgmail.ui.MessageBodyUtils;
import com.subgraph.sgmail.ui.Resources;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import javax.mail.Message;

public class MessageBodyViewer extends Composite {

	public MessageBodyViewer(Composite parent, Message message) {
		super(parent, SWT.NONE);

        setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
        GridLayout layout = new GridLayout();
        layout.marginLeft = 26;
        layout.marginRight = 10;
        layout.marginTop = 10;
        layout.marginBottom = 10;
        setLayout(layout);

        StyledText text = new StyledText(this, SWT.WRAP | SWT.READ_ONLY);

		text.setFont(JFaceResources.getFont(Resources.FONT_MESSAGE_BODY));
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final String body = MessageBodyUtils.getTextBody(message);
		if(body != null) {
			text.setText(body);
		}
	}
}
