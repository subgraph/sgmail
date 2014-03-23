package com.subgraph.sgmail.events;


import com.subgraph.sgmail.search.SearchResult;

public class SearchFilterEvent {
    public static SearchFilterEvent create(SearchResult searchResult) {
       return new SearchFilterEvent(searchResult, false);
    }

    public static SearchFilterEvent createFilterClearEvent() {
        return new SearchFilterEvent(null, true);
    }

    private final SearchResult searchResult;
    private final boolean isFilterClear;

    private SearchFilterEvent(SearchResult searchResult, boolean isFilterClear) {
        this.searchResult = searchResult;
        this.isFilterClear = isFilterClear;
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public boolean isFilterClearEvent() {
        return isFilterClear;
    }
}
