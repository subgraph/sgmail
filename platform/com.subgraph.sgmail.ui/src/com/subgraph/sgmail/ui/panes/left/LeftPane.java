package com.subgraph.sgmail.ui.panes.left;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ca.odell.glazedlists.GroupingList;

import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.accounts.Account;
import com.subgraph.sgmail.database.Model;
import com.subgraph.sgmail.events.ConversationSourceSelectedEvent;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.messages.StoredMessageLabel;
import com.subgraph.sgmail.ui.Resources;

public class LeftPane extends Composite {

	private final IEventBus eventBus;
	
	private final TreeViewer accountsTree;
    private final SearchMatcherEditor searchMatcherEditor;
    private final Map<Object, EventListStack> eventListStackMap = new HashMap<>();

    private EventListStack currentStack;

	public LeftPane(Composite parent, IEventBus eventBus, Model model) {
		super(parent, SWT.NONE);
		this.eventBus = eventBus;
        final GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing= 0;
		setLayout(layout);

        createHeader(this);

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
        searchMatcherEditor = new SearchMatcherEditor(eventBus);
	}

    private void createHeader(Composite parent) {
        final Color white = JFaceResources.getColorRegistry().get(Resources.COLOR_WHITE);
        final Label label = new Label(parent, SWT.CENTER);
        label.setText("Accounts");
        label.setBackground(white);
        label.setForeground(JFaceResources.getColorRegistry().get(Resources.COLOR_HEADER));
        label.setFont(JFaceResources.getFont(Resources.FONT_HEADER));
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        final Label spacer = new Label(parent, SWT.NONE);
        spacer.setBackground(white);
        final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
        gd.heightHint = 10;
        spacer.setLayoutData(gd);
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
		final TreeViewer tv = new TreeViewer(parent, SWT.V_SCROLL | SWT.H_SCROLL);
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
                	eventBus.post(new ConversationSourceSelectedEvent(els.getGroupingList()));
                }
            }
        });
		return tv;
	}

    private void newEventListStack(EventListStack els) {
        final GroupingList<StoredMessage> group = els.addGroupingList();
        eventBus.post(new ConversationSourceSelectedEvent(group));
        if(currentStack != null) {
            currentStack.dispose();
        }
        currentStack = els;
    }

    public boolean isSearchActive() {
    	return searchMatcherEditor.isActive();
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
        if(ob instanceof Account) {
            return EventListStack.createForAccount((Account) ob, searchMatcherEditor);
        } else if(ob instanceof StoredFolder) {
            return EventListStack.createForFolder((StoredFolder) ob, searchMatcherEditor);
        } else if(ob instanceof StoredMessageLabel) {
            return EventListStack.createForLabel((StoredMessageLabel) ob, searchMatcherEditor);
        } else {
            throw new IllegalArgumentException("Cannot produce EventListStack for "+ ob.getClass());
        }
    }
}
