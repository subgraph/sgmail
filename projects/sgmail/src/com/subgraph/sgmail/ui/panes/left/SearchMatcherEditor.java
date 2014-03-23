package com.subgraph.sgmail.ui.panes.left;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import com.google.common.eventbus.Subscribe;
import com.subgraph.sgmail.events.SearchFilterEvent;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.model.Model;
import com.subgraph.sgmail.search.SearchResult;

public class SearchMatcherEditor extends AbstractMatcherEditor<StoredMessage> {
    public static SearchMatcherEditor create(Model model) {
        final SearchMatcherEditor sme = new SearchMatcherEditor();
        model.registerEventListener(sme);
        return sme;
    }

    private class SearchMatcher implements Matcher<StoredMessage> {
        private final SearchResult searchResult;
        SearchMatcher(SearchResult searchResult) {
            this.searchResult = searchResult;
        }
        @Override
        public boolean matches(StoredMessage message) {
            return searchResult.resultContainsUID(message.getUniqueMessageId());
        }
    }

    private SearchMatcherEditor() {

    }

    @Subscribe
    public void onSearchFilter(SearchFilterEvent event) {
        if(event.isFilterClearEvent()) {
            fireMatchAll();
        } else {
            fireChanged(new SearchMatcher(event.getSearchResult()));
        }
    }
}
