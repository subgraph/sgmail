package com.subgraph.sgmail.ui.preferences;

import com.subgraph.sgmail.model.Preferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;

import com.subgraph.sgmail.model.Account;
import com.subgraph.sgmail.model.IMAPAccount;
import com.subgraph.sgmail.model.Model;

public class PreferenceDialogFactory {


	public PreferenceDialog createPreferenceDialog(Shell shell, Model model) {
		PreferenceManager manager = new PreferenceManager();
		PreferenceNode debugNode = new PreferenceNode("debug", new DebugPreferencePage());
		PreferenceNode torNode = new PreferenceNode("tor", new TorPreferencePage());
		manager.addToRoot(debugNode);
        manager.addToRoot(torNode);
		PreferenceNode accountsNode = new PreferenceNode("accounts", new AllAccountsPage());
		
		manager.addToRoot(accountsNode);
		for(Account a: model.getAccounts()) {
			if(a instanceof IMAPAccount) {
				IMAPAccount imap = (IMAPAccount) a;
				PreferenceNode node = new PreferenceNode(imap.getEmailAddress(), new AccountPreferencePage(imap));
				manager.addTo("accounts", node);
			}
		}
		
		
		final PreferenceDialog dialog = new PreferenceDialog(shell, manager);
		final IPreferenceStore store = new PreferenceStoreAdapter(model.getRootStoredPreferences());
		dialog.setPreferenceStore(store);
        System.out.println("tor enabled: "+ store.getBoolean(Preferences.TOR_ENABLED));
        System.out.println("tor port: "+ store.getInt(Preferences.TOR_SOCKS_PORT));
        return dialog;
	}

}
