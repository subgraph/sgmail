package com.subgraph.sgmail.ui.panes.right;

import javax.mail.Message;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.subgraph.sgmail.ui.MessageBodyUtils;
import org.eclipse.swt.widgets.Text;

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

        Text text = new Text(this, SWT.WRAP | SWT.READ_ONLY);
		text.setFont(JFaceResources.getTextFont());
        text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		final String body = MessageBodyUtils.getTextBody(message);
		if(body != null) {
			text.setText(body);
		}
	}
}
