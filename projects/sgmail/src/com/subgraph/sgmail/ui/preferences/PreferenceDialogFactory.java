package com.subgraph.sgmail.ui.preferences;

import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.accounts.IMAPAccount;
import com.subgraph.sgmail.model.Model;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;

import java.util.List;

public class PreferenceDialogFactory {


	public PreferenceDialog createPreferenceDialog(Shell shell, Model model) {
		PreferenceManager manager = new PreferenceManager();
		PreferenceNode debugNode = new PreferenceNode("debug", new DebugPreferencePage());
		PreferenceNode torNode = new PreferenceNode("tor", new TorPreferencePage());
		manager.addToRoot(debugNode);
        manager.addToRoot(torNode);

        addAccountsNodes(manager, model.getAccountList().getAccounts());


		final PreferenceDialog dialog = new PreferenceDialog(shell, manager);
		final IPreferenceStore store = new PreferenceStoreAdapter(model.getRootStoredPreferences());
		dialog.setPreferenceStore(store);
        return dialog;
	}

    private void addAccountsNodes(PreferenceManager manager, List<Account> accounts) {
        if(accounts.isEmpty()) {
            return;
        }
        final PreferenceNode accountsNode = new PreferenceNode("accounts", new AllAccountsPage());
        manager.addToRoot(accountsNode);
        for(Account a: accounts) {
            if(a instanceof IMAPAccount) {
                addIMAPAccount(manager, (IMAPAccount) a);
            }
        }
    }

    private void addIMAPAccount(PreferenceManager manager, IMAPAccount imapAccount) {
        final PreferenceNode node = new PreferenceNode(imapAccount.getEmailAddress(), new AccountPreferencePage(imapAccount));
        manager.addTo("accounts", node);
    }

}
