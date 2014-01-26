package com.subgraph.sgmail.ui.panes.left;

import javax.mail.Folder;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

import com.subgraph.sgmail.model.Account;
import com.subgraph.sgmail.model.GmailLabel;
import com.subgraph.sgmail.model.StoredFolder;
import com.subgraph.sgmail.ui.ImageCache;

public class AccountsLabelProvider  extends LabelProvider {
	
	@Override
	public String getText(Object element) {
		if(element instanceof StoredFolder) {
			final StoredFolder folder = (StoredFolder) element;
			return folder.getFullName();
		} else if(element instanceof Account) {
			return ((Account)element).getLabel();
		} else if(element instanceof GmailLabel) { 
			
			String label = ((GmailLabel) element).getName();
			if(label.startsWith("\\")) {
				return label.substring(1);
			} else {
				return label;
			}
		} else {
			System.out.println("element: "+ element);
			return "???";
		}
	}
	
	@Override
	public Image getImage(Object element) {
		if(element instanceof Account) {
			return ImageCache.getInstance().getImage(ImageCache.INBOX_IMAGE);
		} else if(element instanceof Folder) {
			return ImageCache.getInstance().getImage(ImageCache.FOLDER_IMAGE);
		} else if(element instanceof GmailLabel) { 
			return ImageCache.getInstance().getImage(ImageCache.TAG_IMAGE);
		} else {
			return null;
		}
	}
}
