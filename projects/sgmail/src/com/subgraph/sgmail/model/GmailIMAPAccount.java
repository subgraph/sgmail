package com.subgraph.sgmail.model;

import java.util.List;

import com.db4o.activation.ActivationPurpose;
import com.db4o.collections.ActivatableArrayList;
import com.google.common.collect.ImmutableList;
import com.subgraph.sgmail.events.LabelAddedEvent;

public class GmailIMAPAccount extends IMAPAccount {
	private final static String GMAIL_IMAP_SERVER = "imap.gmail.com";
	
	private List<GmailLabel> labels = new ActivatableArrayList<>();
	
	public GmailIMAPAccount(Model model, String label, String username, String domain, String realname, String password, SMTPAccount smtpAccount) {
		super(model, label, username, domain, realname, password, GMAIL_IMAP_SERVER, smtpAccount);
	}
	
	public List<GmailLabel> getLabels() {
		activate(ActivationPurpose.READ);
		synchronized(labels) {
			return ImmutableList.copyOf(labels);
		}
	}
	
	public GmailLabel getLabelByName(String name) {
		activate(ActivationPurpose.READ);
		synchronized(labels) {
			final GmailLabel label = findLabelByName(name);
			return (label != null) ? (label) : (createNewLabel(name));
		}
	}

	private GmailLabel findLabelByName(String name) {
		for(GmailLabel label: labels) {
			if(label.getName().equals(name)) {
				return label;
			}
		}
		return null;
	}
	
	private GmailLabel createNewLabel(String name) {
		final GmailLabel newLabel = new GmailLabel(this, name);
		model.store(newLabel.getConversationCollection());
		model.store(newLabel);
		labels.add(newLabel);
		model.postEvent(new LabelAddedEvent(newLabel));
		return newLabel;
	}

	protected String getProto() {
		return isSecure() ? "gimaps" : "gimap";
	}
}
