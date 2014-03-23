package com.subgraph.sgmail.ui;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.subgraph.sgmail.events.SearchFilterEvent;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.search.SearchResult;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import java.util.logging.Level;
import java.util.logging.Logger;

public class SearchBarContribution extends ControlContribution {

    private final static Logger logger = Logger.getLogger(SearchBarContribution.class.getName());

    private final Model model;
    private Text searchText;

    public SearchBarContribution(Model model) {
        super("search");
        this.model = model;
    }

    @Override
    protected Control createControl(Composite parent) {
        searchText = new Text(parent, SWT.SEARCH | SWT.SINGLE | SWT.BORDER | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
        searchText.setData(GlobalKeyboardShortcuts.DISABLE_KEYS_WHEN_FOCUSED, Boolean.TRUE);
        searchText.addSelectionListener(createSelectionListener());
        searchText.addModifyListener(createModifyListener());
        return searchText;
    }

    protected int computeWidth(Control control) {
        return 200;
    }

    private ModifyListener createModifyListener() {
        return new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                final String s = searchText.getText();
                if(!s.isEmpty()) {
                    onTextChanged(s);
                } else {
                    model.postEvent(SearchFilterEvent.createFilterClearEvent());
                }
            }
        };
    }

    private SelectionListener createSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                if(e.detail == SWT.CANCEL) {
                    // Cancel icon clicked
                    model.postEvent(SearchFilterEvent.createFilterClearEvent());
                }
            }
        };
    }

    private void onTextChanged(String searchText) {
        final SearchTask task = new SearchTask(model.getMessageSearchIndex(), searchText);
        final ListenableFuture<SearchResult> future = model.submitTask(task);
        Futures.addCallback(future, createFutureCallback());
    }

    private FutureCallback<SearchResult> createFutureCallback() {
        return new FutureCallback<SearchResult>() {
            @Override
            public void onSuccess(SearchResult searchResult) {
                System.out.println("Search: '"+ searchResult.getQueryText() +"'");
                System.out.println("Search completed successfully with "+ searchResult.getMatchCount() + " matches");
                System.out.println(searchResult);
                model.postEvent(SearchFilterEvent.create(searchResult));
            }

            @Override
            public void onFailure(Throwable throwable) {
                logger.log(Level.WARNING, "Search query failed with exception "+ throwable.getMessage(), throwable);
            }
        };
    }
}
