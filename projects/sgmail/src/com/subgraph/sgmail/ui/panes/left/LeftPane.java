package com.subgraph.sgmail.ui.panes.left;

import ca.odell.glazedlists.GroupingList;
import ca.odell.glazedlists.impl.matchers.TrueMatcher;
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

import java.util.HashMap;
import java.util.Map;

public class LeftPane extends Composite {

	private final Model model;
	
	private final TreeViewer accountsTree;
    private final SearchMatcherEditor searchMatcherEditor;
    private final Map<Object, EventListStack> eventListStackMap = new HashMap<>();

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
        searchMatcherEditor = new SearchMatcherEditor(model);
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
		tv.setLabelProvider(new LabelProvider(this));
		tv.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                final StructuredSelection ss = (StructuredSelection) event.getSelection();
                final Object ob = ss.getFirstElement();
                if (ob != null) {
                    final EventListStack els = getEventListStackFor(ob);
                    model.postEvent(new ConversationSourceSelectedEvent(els.getGroupingList()));
                }
            }
        });
		return tv;
	}

    private void newEventListStack(EventListStack els) {
        final GroupingList<StoredMessage> group = els.addGroupingList();
        model.postEvent(new ConversationSourceSelectedEvent(group));
        if(currentStack != null) {
            currentStack.dispose();
        }
        currentStack = els;
    }

    public boolean isSearchActive() {
        return !(searchMatcherEditor.getMatcher() instanceof TrueMatcher);
    }

    public EventListStack getEventListStackFor(Object ob) {
        synchronized (eventListStackMap) {
            if(!eventListStackMap.containsKey(ob)) {
                final EventListStack els = generateEventListStackFor(ob);
                els.getNewMessageCounter().addPropertyChangeListener(e -> refreshTree());
                eventListStackMap.put(ob, els);
            }
        }
        return eventListStackMap.get(ob);
    }

    private EventListStack generateEventListStackFor(Object ob) {
        if(ob instanceof IMAPAccount) {
            return EventListStack.createForIMAPAccount((IMAPAccount) ob, searchMatcherEditor);
        } else if(ob instanceof StoredFolder) {
            return EventListStack.createForFolder((StoredFolder) ob, searchMatcherEditor);
        } else if(ob instanceof StoredMessageLabel) {
            return EventListStack.createForLabel((StoredMessageLabel) ob, searchMatcherEditor);
        } else {
            throw new IllegalArgumentException("Cannot produce EventListStack for "+ ob.getClass());
        }
    }
}
