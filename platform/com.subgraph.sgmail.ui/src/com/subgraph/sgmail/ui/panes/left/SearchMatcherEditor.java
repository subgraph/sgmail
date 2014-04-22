package com.subgraph.sgmail.ui.panes.left;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;

import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.events.SearchFilterEvent;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.search.SearchResult;

public class SearchMatcherEditor extends AbstractMatcherEditor<StoredMessage> {

    private class SearchMatcher implements Matcher<StoredMessage> {
        private final SearchResult searchResult;
        SearchMatcher(SearchResult searchResult) {
            this.searchResult = searchResult;
        }
        @Override
        public boolean matches(StoredMessage message) {
            return searchResult.resultContainsMessageId(message.getMessageId());
        }
    }

    private final IEventBus eventBus;
    private volatile boolean isActive;
    public SearchMatcherEditor(IEventBus eventBus) {
    	this.eventBus = eventBus;
    	eventBus.register(this);
    }

    public void dispose() {
    	eventBus.unregister(this);
    }
    
    public boolean isActive() {
    	return isActive;
    }

    @Subscribe
    public void onSearchFilter(SearchFilterEvent event) {
        if(event.isFilterClearEvent()) {
        	isActive = false;
            fireMatchAll();
            
        } else {
        	isActive = true;
            fireChanged(new SearchMatcher(event.getSearchResult()));
        }
    }
}
