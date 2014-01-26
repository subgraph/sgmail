package com.subgraph.sgmail.ui.panes.middle;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.events.ConversationAddedEvent;
import com.subgraph.sgmail.events.ConversationSelectedEvent;
import com.subgraph.sgmail.events.ConversationSourceSelectedEvent;
import com.subgraph.sgmail.events.MessageStateChangedEvent;
import com.subgraph.sgmail.events.NextConversationEvent;
import com.subgraph.sgmail.model.Conversation;
import com.subgraph.sgmail.model.ConversationSource;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.ui.MainWindow;

public class MiddlePane extends Composite {
	private final TableViewer tableViewer;
	
	private ConversationSource currentSource;
	
	public MiddlePane(Composite parent, MainWindow main, final Model model) {
		super(parent, SWT.NONE);
		setLayout(new GridLayout());
		
		final Label label = new Label(this, SWT.NONE);
		label.setText("Conversations");
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		Composite tableComposite = new Composite(this, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer = new TableViewer(tableComposite, SWT.NONE);
		
		createColumns(tableComposite, tableViewer);
		tableViewer.setContentProvider(new ContentProvider());
		tableViewer.setLabelProvider(new ConversationLabelProvider(tableViewer));
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final StructuredSelection ss = (StructuredSelection) event.getSelection();
				final Object ob = ss.getFirstElement();
				if(ob instanceof Conversation) {
					model.postEvent(new ConversationSelectedEvent((Conversation) ob));
				}
			}
		});
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event) {
				System.out.println("clikityclick");
				// TODO Auto-generated method stub
				
			}
		});
		model.registerEventListener(this);
	}

	@Subscribe
	public void onMessageStateChanged(MessageStateChangedEvent event) {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				tableViewer.refresh();
			}
		});
	}
	
	@Subscribe
	public void onConversationAdded(final ConversationAddedEvent event) {
		if(event.getSource() == currentSource) {
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					tableViewer.setInput(event.getSource());
				}
			});
		}
	}

	@Subscribe
	public void onConversationSourceSelected(ConversationSourceSelectedEvent event) {
		currentSource = event.getSelectedSource();
		tableViewer.setInput(event.getSelectedSource());
	}
	
	@Subscribe
	public void onNextConversation(NextConversationEvent event) {
		getDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				selectNextConversation();
			}
		});
	}
	
	private void selectNextConversation() {
		StructuredSelection ss = (StructuredSelection) tableViewer.getSelection();
		Object next = getNextElement(ss.getFirstElement());
		if(next != null) {
			tableViewer.setSelection(new StructuredSelection(next), true);
		}
	}
	
	private Object getNextElement(Object current) {
		if(currentSource == null) {
			return null;
		}

		boolean seenCurrent = (current == null);
		for(Conversation c: currentSource.getConversations()) {
			if(c == current) {
				seenCurrent = true;
			} else {
				if(seenCurrent && c.hasUndeletedMessages()) {
					return c;
				}
			}
		}
		return null;
	}
	
	private void createColumns(Composite tableComposite, TableViewer tv) {
		final TableColumnLayout layout = new TableColumnLayout();
		final Table table = tv.getTable();
		tableComposite.setLayout(layout);
		table.setLinesVisible(false);
		final TableColumn tc = new TableColumn(table, SWT.NONE, 0);
		layout.setColumnData(tc, new ColumnWeightData(100));
		tc.setText("Convs");
	}
}
