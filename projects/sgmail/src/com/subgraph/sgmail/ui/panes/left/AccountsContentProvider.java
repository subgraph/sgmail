package com.subgraph.sgmail.ui.panes.left;

import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.event.ListEventListener;
import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.model.AccountList;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AccountsContentProvider implements ITreeContentProvider, ListEventListener<Account> {
	private final static Logger logger = Logger.getLogger(AccountsContentProvider.class.getName());

    private AccountList accountList;
    private Viewer accountsViewer;

	@Override
	public void dispose() {
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if(accountList != null) {
            accountList.getAccounts().removeListEventListener(this);
        }

        accountList = (AccountList) newInput;

        if(accountList != null) {
            accountList.getAccounts().addListEventListener(this);
        }

        accountsViewer = viewer;
        accountsViewer.refresh();
	}

	@Override
	public Object[] getElements(Object inputElement) {
        return getRootElements();
	}

	private Object[] getRootElements() {
        if(accountList == null) {
            return new Object[0];
        }
		final List<Object> elems = new ArrayList<>();
        for(Account a: accountList.getAccounts()) {
            elems.add(a);
            elems.addAll(a.getMessageLabels());
        }
        return elems.toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
        if(parentElement instanceof AccountList) {
			return getRootElements();
		} else if(parentElement instanceof Account) {
			return getChildrenOfAccount((Account) parentElement);
		} else {
			return null;
		}
	}

	private Object[] getChildrenOfAccount(Account account) {
        final List<Object> result = new ArrayList<>();
		for(StoredFolder f: account.getFolders()) {
			result.add(f);
		}
		return result.toArray();
	}
	

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
        if(element instanceof AccountList) {
            return !(accountList == null || accountList.getAccounts().isEmpty());
		} else if(element instanceof Account) {
			return getChildrenOfAccount((Account) element).length > 0;
		} else {
			return false;
		}
	}

    @Override
    public void listChanged(ListEvent<Account> listChanges) {
        if(accountsViewer != null) {
            accountsViewer.getControl().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if(!accountsViewer.getControl().isDisposed()) {
                        accountsViewer.refresh();
                    }
                }
            });
        }
    }
}
