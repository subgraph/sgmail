package com.subgraph.sgmail.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.subgraph.sgmail.servers.ServerInformation;
import com.subgraph.sgmail.servers.ServerInformation.Protocol;

public class IMapServerInfoPanel extends Composite {

    private final boolean useTor;
	private ServerInformation incomingServer;
	private ServerInformation outgoingServer;
	
	private Label incomingProtocol;
	private Label incomingHost;
	private Label smtpHost;
	
	IMapServerInfoPanel(Composite parent, boolean useTor) {
		super(parent, SWT.NONE);
        this.useTor = useTor;
		setLayout(new FillLayout());
		final Group g = new Group(this, SWT.NONE);
		g.setText("Server Information");
		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 20;
		g.setLayout(layout);
		create(g);
	}

	public void clearServerInfo() {
		incomingProtocol.setText("");
		incomingHost.setText("");
		smtpHost.setText("");
	}
	
	public ServerInformation getIncomingServer() {
		return incomingServer;
	}
	
	public ServerInformation getOutgoingServer() {
		return outgoingServer;
	}

	public void setServerInfo(ServerInformation incoming, ServerInformation outgoing) {
		this.incomingServer = incoming;
		this.outgoingServer = outgoing;
		if(incoming != null) {
			incomingProtocol.setText(getProtocolString(incoming.getProtocol()));
			incomingHost.setText(getHostString(incoming));
		}
		if(outgoing != null) {
			smtpHost.setText(getHostString(outgoing));
		}
	}
	
	private String getHostString(ServerInformation info) {
        if(useTor && info.getOnionHostname() != null) {
            return info.getOnionHostname() + ":" + Integer.toString(info.getPort());
        } else {
            return info.getHostname() +":"+ Integer.toString(info.getPort());
        }
	}
	
	private String getProtocolString(Protocol protocol) {
		
		switch(protocol) {
		case IMAP:
			return "imap";
			
		case POP3:
			return "pop3";

			
		case UNKNOWN:
			return "unknown";
			
		case SMTP:
			return "smtp";
		
		default:
			return "";
		}
	}
	
	private Label createLabelPair(Composite parent, String labelText) {
		final Label labelLabel = new Label(parent, SWT.NONE);
		labelLabel.setText(labelText);
		labelLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));
		
		final Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		return label;
	}
	
	private void create(Composite parent) {
		incomingProtocol = createLabelPair(parent, "Incoming protocol:");
		incomingHost = createLabelPair(parent, "Incoming server:");
		smtpHost = createLabelPair(parent, "SMTP server:");
		clearServerInfo();
	}
}
