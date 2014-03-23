package com.subgraph.sgmail.ui.panes.left;

import ca.odell.glazedlists.GroupingList;
import com.subgraph.sgmail.accounts.IMAPAccount;
import com.subgraph.sgmail.events.ConversationSourceSelectedEvent;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.messages.StoredMessageLabel;
import com.subgraph.sgmail.model.Model;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LeftPane extends Composite {

	private final Model model;
	
	private final TreeViewer accountsTree;
    private final SearchMatcherEditor searchMatcherEditor;

    private EventListStack currentStack;

	public LeftPane(Composite parent, Model model) {
		super(parent, SWT.NONE);
		this.model = model;
		setLayout(new GridLayout());
		final Label label = new Label(this, SWT.NONE);
		label.setText("Mailboxes");
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		accountsTree = createTreeViewer(this);
        addControlListener(new ControlAdapter() {
            @Override
            public void controlMoved(ControlEvent e) {

            }

            @Override
            public void controlResized(ControlEvent e) {
                accountsTree.getTree().redraw();
                accountsTree.refresh(true);

            }
        });
        layout();
        accountsTree.setInput(model.getAccountList());
        searchMatcherEditor = SearchMatcherEditor.create(model);
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
		tv.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                final StructuredSelection ss = (StructuredSelection) event.getSelection();
                final Object ob = ss.getFirstElement();
                if (ob instanceof IMAPAccount) {
                    onIMAPAccountSelected((IMAPAccount) ob);
                } else if (ob instanceof StoredFolder) {
                    onStoredFolderSelected((StoredFolder) ob);
                } else if (ob instanceof StoredMessageLabel) {
                    onStoredMessageLabelSelected((StoredMessageLabel) ob);
                }
            }
        });
		return tv;
	}

    private void onIMAPAccountSelected(IMAPAccount imapAccount) {
        final EventListStack els = new EventListStack(imapAccount.getMessageEventList());
        els.addSearchFilter(searchMatcherEditor);
        newEventListStack(els);
    }

    private void onStoredFolderSelected(StoredFolder folder) {
        final EventListStack els = new EventListStack(folder.getMessageEventList());
        els.addSearchFilter(searchMatcherEditor);
        newEventListStack(els);
    }

    private void onStoredMessageLabelSelected(StoredMessageLabel label) {
        if(!(label.getAccount() instanceof IMAPAccount)) {
            return;
        }
        final IMAPAccount imapAccount = (IMAPAccount) label.getAccount();
        final EventListStack els = new EventListStack(imapAccount.getMessageEventList());
        els.addLabelFilter(label);
        els.addSearchFilter(searchMatcherEditor);
        newEventListStack(els);
    }

    private void newEventListStack(EventListStack els) {
        final GroupingList<StoredMessage> group = els.addGroupingList();
        model.postEvent(new ConversationSourceSelectedEvent(group));
        if(currentStack != null) {
            currentStack.dispose();
        }
        currentStack = els;
    }
}
