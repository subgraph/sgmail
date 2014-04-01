package com.subgraph.sgmail.search;

public interface SearchResult {
    String getQueryText();
    int getMatchCount();
    boolean resultContainsMessageId(int id);
    HighlightedString getHighlightedSubject(int uid);
    HighlightedString getHighlightedBody(int uid);
    void dispose();
}
