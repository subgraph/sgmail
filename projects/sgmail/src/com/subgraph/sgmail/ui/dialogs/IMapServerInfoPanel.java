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

	private ServerInformation incomingServer;
	private ServerInformation outgoingServer;
	
	private Label serverInfoLabel;
	
	IMapServerInfoPanel(Composite parent) {
		super(parent, SWT.NONE);
		setLayout(new FillLayout());
		final Group g = new Group(this, SWT.NONE);
		g.setText("Server Information");
		g.setLayout(new GridLayout());
		create(g);
	}

	public void clearServerInfo() {
		serverInfoLabel.setText("");
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
		
		StringBuilder sb = new StringBuilder();
		sb.append("Incoming Server\n\n");
		addProtocol(sb, incoming.getProtocol());
		sb.append("Hostname:\t\t"+ incoming.getHostname() + "\n");
		sb.append("Port:\t\t\t\t"+ incoming.getPort() + "\n");
		sb.append("\nOutgoing Server\n\n");
		sb.append("Hostname:\t\t"+ outgoing.getHostname() + "\n");
		sb.append("Port:\t\t\t\t"+ outgoing.getPort()+"\n");
		serverInfoLabel.setText(sb.toString());
	}
	
	private void addProtocol(StringBuilder sb, Protocol protocol) {
		sb.append("Protocol:\t\t\t");
		switch(protocol) {
		case IMAP:
			sb.append("imap");
			break;
			
		case POP3:
			sb.append("pop3");
			break;
			
		case UNKNOWN:
			sb.append("unknown");
			break;
			
		case SMTP:
			sb.append("smtp");
			break;
		
		default:
			break;
		}
		sb.append("\n");
	}
	
	private void create(Composite parent) {
		serverInfoLabel = new Label(parent, SWT.NONE);
		serverInfoLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

}
