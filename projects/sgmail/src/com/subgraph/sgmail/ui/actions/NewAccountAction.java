package com.subgraph.sgmail.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.subgraph.sgmail.model.GmailIMAPAccount;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.SMTPAccount;
import com.subgraph.sgmail.servers.ServerInformation;
import com.subgraph.sgmail.ui.dialogs.NewAccountDialog;

public class NewAccountAction extends Action {
	
	private final Model model;
	
	public NewAccountAction(Model model) {
		super("New Account");
		this.model = model;
	}
	
	public void run() {
		final Shell shell = Display.getCurrent().getActiveShell();
		NewAccountDialog dialog = new NewAccountDialog(shell);
		
		if(dialog.open() == Window.OK) {
			processNewAccount(dialog.getIncomingServer(), dialog.getOutgoingServer(), dialog.getUsername(), dialog.getDomain(), dialog.getRealname(), dialog.getPassword());
		} 
	}
	
	private void processNewAccount(ServerInformation incoming, ServerInformation outgoing, String username, String domain, String realname, String password) {
		SMTPAccount smtp = createSMTPAccount(outgoing, username, password);
		model.store(smtp);
		GmailIMAPAccount account = new GmailIMAPAccount(model, username + "@" + domain, username, domain, realname, password, smtp);
		model.addAccount(account);
	}
	
	private SMTPAccount createSMTPAccount(ServerInformation outgoingServer, String username, String password) {
		return new SMTPAccount(outgoingServer.getHostname(), outgoingServer.getPort(), username, password);
	}
}
