package com.subgraph.sgmail.events;

public class SearchQueryChangedEvent {
    private final String searchQuery;

    public SearchQueryChangedEvent(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getSearchQuery() {
        return searchQuery;
    }
}
