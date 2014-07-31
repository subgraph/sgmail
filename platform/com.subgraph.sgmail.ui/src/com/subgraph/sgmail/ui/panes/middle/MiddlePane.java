package com.subgraph.sgmail.ui.panes.middle;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.swt.GlazedListsSWT;
import ca.odell.glazedlists.swt.TableItemConfigurer;

import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.events.ConversationSelectedEvent;
import com.subgraph.sgmail.events.ConversationSourceSelectedEvent;
import com.subgraph.sgmail.events.MessageStateChangedEvent;
import com.subgraph.sgmail.events.SearchFilterEvent;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.ui.utils.glazed.ConversationEventTableViewer;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.List;

public class MiddlePane extends Composite {

    private final Table table;
    private final TableColumnLayout tableColumnLayout;
    private final ConversationRenderer conversationRenderer;
    private final IEventBus eventBus;

    private final Color selectedBackground;
    private final Color selectedShadow;
    private final Color selectedTopLine;
    private final Color selectedBottomLine;

    private ConversationEventTableViewer<List<StoredMessage>> eventTableViewer;

    public MiddlePane(Composite parent, IEventBus eventBus) {
        super(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        setLayout(layout);

        final Composite tableComposite = new Composite(this, SWT.NONE);
        tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        this.table = createTable(tableComposite);
        tableColumnLayout = new TableColumnLayout();
        tableComposite.setLayout(tableColumnLayout);
        conversationRenderer = new ConversationRenderer(getDisplay());
        this.eventBus = eventBus;
        this.selectedBackground = createGrey(236);
        this.selectedShadow = createGrey(219);
        this.selectedTopLine = createGrey(155);
        this.selectedBottomLine = createGrey(222);

        eventBus.register(this);
    }

    private Color createGrey(int n) {
        return new Color(getDisplay(), n, n, n);

    }

    @Subscribe
    public void onConversationSourceSelected(ConversationSourceSelectedEvent event) {
        if(eventTableViewer != null) {
            eventTableViewer.dispose();
            table.removeAll();
        }
        eventTableViewer = createEventTableViewer(event.getSelectedSource());
    }

    @Subscribe
    public void onSearchFilterChanged(SearchFilterEvent event) {
        if(event.isFilterClearEvent()) {
            conversationRenderer.setSearchResult(null);
        } else {
            conversationRenderer.setSearchResult(event.getSearchResult());
        }
        getDisplay().asyncExec(() -> table.redraw());
    }
    
    @Subscribe
    public void onMessageStateChanged(MessageStateChangedEvent event) {
      getDisplay().asyncExec(() -> table.redraw());
    }

    private ConversationEventTableViewer<List<StoredMessage>> createEventTableViewer(EventList<List<StoredMessage>> sourceList) {
            return new ConversationEventTableViewer<>(createProxySource(sourceList), table, new TableFormatter(), TableItemConfigurer.DEFAULT, true);
    }

    private EventList<List<StoredMessage>> createProxySource(EventList<List<StoredMessage>> sourceList) {
        sourceList.getReadWriteLock().readLock().lock();
        try {
            return GlazedListsSWT.swtThreadProxyList(sourceList, getDisplay());
        } finally {
            sourceList.getReadWriteLock().readLock().unlock();
        }
    }

    private Table createTable(Composite tableComposite) {
        final Table table = new Table(tableComposite, SWT.V_SCROLL);
        final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.setLayoutData(gd);
        final Listener listener = createEventListener();
        table.addListener(SWT.MeasureItem, listener);
        table.addListener(SWT.EraseItem, listener);
        table.addListener(SWT.PaintItem, listener);
        table.addSelectionListener(createSelectionListener());
        return table;
    }

    private SelectionListener createSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                @SuppressWarnings("unchecked")
				final List<StoredMessage> messages = (List<StoredMessage>) e.item.getData();
                if(messages != null) {
                    eventBus.post(new ConversationSelectedEvent(messages));
                }
            }
        };
    }

    private Listener createEventListener() {
        return new Listener() {
            @Override
            public void handleEvent(Event event) {
                switch(event.type) {
                    case SWT.MeasureItem:
                        onMeasureItem(event);
                        break;
                    case SWT.EraseItem:
                        onEraseItem(event);
                        break;
                    case SWT.PaintItem:
                        onPaintItem(event);
                        break;
                }
            }
        };
    }

    private void onMeasureItem(Event event) {
        event.width = table.getClientArea().width;
        event.height = conversationRenderer.getTotalHeight();
    }

    private void onEraseItem(Event event) {
        if((event.detail & SWT.SELECTED) == 0) {
            return;
        }
        int clientWidth = table.getClientArea().width;
        event.gc.setForeground(selectedShadow);
        event.gc.setBackground(selectedBackground);
        event.gc.fillRectangle(0, event.y, clientWidth, event.height);
        event.gc.fillGradientRectangle(0, event.y + 1, clientWidth, 5, true);
        event.gc.setForeground(selectedTopLine);
        event.gc.drawLine(0, event.y, clientWidth, event.y);
        event.gc.setForeground(selectedBottomLine);
        int y = event.y + event.height;
        event.gc.drawLine(0, y, clientWidth, y);
        event.detail &= ~SWT.SELECTED;
    }


    private void onPaintItem(Event event) {
        event.width = table.getClientArea().width;
        @SuppressWarnings("unchecked")
		final List<StoredMessage> messages = (List<StoredMessage>) event.item.getData();
        conversationRenderer.renderAll(event, messages);
    }
}
