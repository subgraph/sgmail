package com.subgraph.sgmail.ui.panes.left;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.mail.Folder;
import javax.mail.MessagingException;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.subgraph.sgmail.model.Account;
import com.subgraph.sgmail.model.GmailIMAPAccount;
import com.subgraph.sgmail.model.IMAPAccount;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.model.StoredFolder;

public class AccountsContentProvider implements ITreeContentProvider {
	private final static Logger logger = Logger.getLogger(AccountsContentProvider.class.getName());
	
	private Model model;

	@Override
	public void dispose() {
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.model = (Model) newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if(model == null) {
			return new Object[0];
		} else {
			return getRootElements();
			//return model.getAccounts().toArray();
		}
	}
	
	private Object[] getRootElements() {
		final List<Object> elems = new ArrayList<>();
		for(Account a: model.getAccounts()) {
			elems.add(a);
			if(a instanceof GmailIMAPAccount) {
				elems.addAll(((GmailIMAPAccount) a).getLabels());
			}
		}
		return elems.toArray();
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if(parentElement instanceof Model) {
			return getRootElements();
			//return ((Model) parentElement).getAccounts().toArray();
		} else if(parentElement instanceof IMAPAccount) {
			return getChildrenOfAccount((IMAPAccount) parentElement);
		} else if(parentElement instanceof Folder) {
			return getChildrenOfFolder((Folder) parentElement);
		} else {
			return null;
		}
	}

	private Object[] getChildrenOfAccount(IMAPAccount account) {
		final List<Object> result = new ArrayList<>();

		for(StoredFolder f: account.getFolders()) {
			result.add(f);
		}

		//if(account instanceof GmailIMAPAccount) {
//			result.addAll(((GmailIMAPAccount)account).getLabels());
		//}
		return result.toArray();
	}
	
	private Object[] getChildrenOfFolder(Folder folder) {
		try {
			return folder.list();
		} catch (MessagingException e) {
			logger.warning("Exception retrieving child elements of Folder "+ e);
			return new Object[0];
		}
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if(element instanceof Model) {
			return !((Model)element).getAccounts().isEmpty();
		} else if(element instanceof IMAPAccount) {
			return getChildrenOfAccount((IMAPAccount) element).length > 0;
		} else if(element instanceof Folder) {
			return getChildrenOfFolder((Folder) element).length > 0;
		} else {
			return false;
		}
	}
}
