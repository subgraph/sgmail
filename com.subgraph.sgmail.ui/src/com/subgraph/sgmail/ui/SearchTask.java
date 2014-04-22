package com.subgraph.sgmail.ui;

import com.subgraph.sgmail.search.MessageSearchIndex;
import com.subgraph.sgmail.search.SearchResult;

import java.util.concurrent.Callable;

class SearchTask implements Callable<SearchResult> {

    private final MessageSearchIndex messageSearchIndex;
    private final String queryString;

    public SearchTask(MessageSearchIndex messageSearchIndex, String queryString) {
        this.messageSearchIndex = messageSearchIndex;
        this.queryString= queryString;
    }

    @Override
    public SearchResult call() throws Exception {
        return messageSearchIndex.search(queryString);
    }
}
