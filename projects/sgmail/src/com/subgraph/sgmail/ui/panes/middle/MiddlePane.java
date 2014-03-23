package com.subgraph.sgmail.ui.panes.middle;

import org.eclipse.swt.widgets.Composite;

public class MiddlePane extends Composite {
    public MiddlePane(Composite parent, int style) {
        super(parent, style);
    }
    /*
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
		//tableViewer.setLabelProvider(new ConversationLabelProvider(tableViewer));
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				final StructuredSelection ss = (StructuredSelection) event.getSelection();
				final Object ob = ss.getFirstElement();
                System.out.println("ob: "+ ob);
                if(ob instanceof EventList) {
					model.postEvent(new ConversationSelectedEvent((EventList<StoredMessage>) ob));
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
				if(seenCurrent && c.getMessageCount() > 0) {
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
	*/
}
