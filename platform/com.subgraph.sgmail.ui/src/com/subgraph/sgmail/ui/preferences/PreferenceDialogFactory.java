package com.subgraph.sgmail.ui.preferences;

import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;

import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.accounts.AccountList;
import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.database.Preferences;

public class PreferenceDialogFactory {


	public PreferenceDialog createPreferenceDialog(Shell shell, Model model) {
		PreferenceManager manager = new PreferenceManager();
		PreferenceNode debugNode = new PreferenceNode("debug", new DebugPreferencePage());
		PreferenceNode torNode = new PreferenceNode("tor", new TorPreferencePage());
		manager.addToRoot(debugNode);
        manager.addToRoot(torNode);

        final AccountList accountList = model.getAccountList();
        addAccountsNodes(manager, accountList.getAccounts());


        final Preferences prefs = model.getRootPreferences();
		final PreferenceDialog dialog = new PreferenceDialog(shell, manager);
		final IPreferenceStore store = new PreferenceStoreAdapter(prefs);
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
            if(a instanceof MailAccount) {
                addMailAccount(manager, (MailAccount) a);
            }
        }
    }

    private void addMailAccount(PreferenceManager manager, MailAccount imapAccount) {
        final PreferenceNode node = new PreferenceNode(imapAccount.getEmailAddress(), new AccountPreferencePage(imapAccount));
        manager.addTo("accounts", node);
    }

}
