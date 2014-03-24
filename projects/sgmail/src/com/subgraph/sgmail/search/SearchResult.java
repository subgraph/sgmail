package com.subgraph.sgmail.search;

public interface SearchResult {
    String getQueryText();
    int getMatchCount();
    boolean resultContainsUID(long uid);
    HighlightedString getHighlightedSubject(long uid);
    HighlightedString getHighlightedBody(long uid);
    void dispose();
}
