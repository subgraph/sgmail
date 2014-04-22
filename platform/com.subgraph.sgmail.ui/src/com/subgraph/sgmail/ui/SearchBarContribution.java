package com.subgraph.sgmail.ui;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.events.SearchFilterEvent;
import com.subgraph.sgmail.events.SearchQueryChangedEvent;
import com.subgraph.sgmail.search.MessageSearchIndex;
import com.subgraph.sgmail.search.SearchResult;

public class SearchBarContribution extends ControlContribution {

    private final static Logger logger = Logger.getLogger(SearchBarContribution.class.getName());

    private final static int SEARCH_DELAY_MS = 50;

    private final ScheduledExecutorService scheduler =
            MoreExecutors.getExitingScheduledExecutorService((ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1));

    private final Object lock = new Object();
    private final IEventBus eventBus;
    private final MessageSearchIndex messageSearchIndex;
    private final ListeningExecutorService globalExecutor;
    private ScheduledFuture<?> launchSearchFuture;
    private ListenableFuture<SearchResult> pendingSearchFuture;
    private boolean isPendingSearchValid;
    private SearchResult lastResult;

    private final FutureCallback<SearchResult> searchCompletedCallback;

    private Text searchText;

    public SearchBarContribution(IEventBus eventBus, MessageSearchIndex messageSearchIndex, ListeningExecutorService globalExecutor) {
        super("search");
        this.eventBus = eventBus;
        this.messageSearchIndex = messageSearchIndex;
        this.globalExecutor = globalExecutor;
        this.searchCompletedCallback = createFutureCallback();
    }

    @Override
    protected Control createControl(Composite parent) {
        searchText = new Text(parent, SWT.SEARCH | SWT.SINGLE | SWT.BORDER | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
        searchText.setData(GlobalKeyboardShortcuts.DISABLE_KEYS_WHEN_FOCUSED, Boolean.TRUE);
        searchText.addSelectionListener(createSelectionListener());
        searchText.addModifyListener(e -> onTextModify());
        return searchText;
    }

    protected int computeWidth(Control control) {
        return 200;
    }

    private void onTextModify() {
        final String s = searchText.getText();
        if(s.isEmpty()) {
            clearSearch();
        } else {
            delayLaunchSearch(s);
        }
        eventBus.post(new SearchQueryChangedEvent(s));
    }

    private void delayLaunchSearch(String searchQuery) {
        synchronized (lock) {
            cancelPendingSearch();
            launchSearchFuture = scheduler.schedule(() -> launchSearch(searchQuery), SEARCH_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }

    private void cancelPendingSearch() {
        synchronized (lock) {
            if(launchSearchFuture != null) {
                launchSearchFuture.cancel(false);
                launchSearchFuture = null;
            }
            if(pendingSearchFuture != null) {
                pendingSearchFuture.cancel(false);
                pendingSearchFuture = null;
                isPendingSearchValid = false;
            }
        }
    }

    private SelectionListener createSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if(e.detail == SWT.CANCEL) {
                    // Cancel icon clicked
                    clearSearch();
                }
            }
        };
    }

    private void clearSearch() {
        synchronized (lock) {
            cancelPendingSearch();
            lastResult = null;
        }
        eventBus.post(SearchFilterEvent.createFilterClearEvent());
    }

    private void launchSearch(String searchText) {
        final SearchTask task = new SearchTask(messageSearchIndex, searchText);
        synchronized (lock) {
            isPendingSearchValid = true;
            pendingSearchFuture = globalExecutor.submit(task);
            Futures.addCallback(pendingSearchFuture, searchCompletedCallback);
        }
    }


    private void onSearchComplete(SearchResult searchResult) {
        final SearchResult disposeResult;
        synchronized (lock) {
            if(!isPendingSearchValid) {
                searchResult.dispose();
                return;
            }
            if(lastResult != null && lastResult.getMatchCount() > 0) {
                if(searchResult.getMatchCount() == 0) {
                    searchResult.dispose();
                    return;
                }
            }
            disposeResult = lastResult;
            lastResult = searchResult;
        }
        eventBus.post(SearchFilterEvent.create(searchResult));
        if(disposeResult != null) {
            disposeResult.dispose();
        }
    }

    private FutureCallback<SearchResult> createFutureCallback() {
        return new FutureCallback<SearchResult>() {
            @Override
            public void onSuccess(SearchResult searchResult) {
                onSearchComplete(searchResult);
            }

            @Override
            public void onFailure(Throwable throwable) {
                if(throwable instanceof CancellationException) {
                   return;
                }
                logger.log(Level.WARNING, "Search query failed with exception "+ throwable.getMessage(), throwable);
            }
        };
    }
}
