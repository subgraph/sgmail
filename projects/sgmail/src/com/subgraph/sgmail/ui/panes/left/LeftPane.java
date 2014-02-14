package com.subgraph.sgmail.ui.panes.left;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.events.AccountAddedEvent;
import com.subgraph.sgmail.events.ConversationSourceSelectedEvent;
import com.subgraph.sgmail.events.LabelAddedEvent;
import com.subgraph.sgmail.events.MessageStateChangedEvent;
import com.subgraph.sgmail.model.ConversationSource;
import com.subgraph.sgmail.model.Model;

public class LeftPane extends Composite {

	private final Model model;
	
	private final TreeViewer accountsTree;
	
	public LeftPane(Composite parent, Model model) {
		super(parent, SWT.NONE);
		this.model = model;
		setLayout(new GridLayout());
		final Label label = new Label(this, SWT.NONE);
		label.setText("Mailboxes");
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		accountsTree = createTreeViewer(this);
		model.registerEventListener(this);
	}
	
	@Subscribe
	public void onMessageStateChanged(MessageStateChangedEvent event) {
		refreshTree();
	}
	
	@Subscribe
	public void onAccountAdded(AccountAddedEvent event) {
		refreshTree();
	}
	
	@Subscribe
	public void onLabelAdded(LabelAddedEvent event) {
		refreshTree();
	}
	
	private void refreshTree() {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				accountsTree.refresh();
			}
		});
	}

	private TreeViewer createTreeViewer(Composite parent) {
		final TreeViewer tv = new TreeViewer(parent);
		tv.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tv.setContentProvider(new AccountsContentProvider());
		tv.setLabelProvider(new LabelProvider());
		tv.setInput(model);
		tv.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final StructuredSelection ss = (StructuredSelection) event.getSelection();
				final Object ob = ss.getFirstElement();
				if(ob instanceof ConversationSource) {
					model.postEvent(new ConversationSourceSelectedEvent((ConversationSource) ob));
				}
			}
		});
		return tv;
	}

}
