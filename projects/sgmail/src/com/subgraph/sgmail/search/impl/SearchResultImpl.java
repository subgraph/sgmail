package com.subgraph.sgmail.search.impl;

import com.subgraph.sgmail.search.HighlightedString;
import com.subgraph.sgmail.search.SearchResult;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class SearchResultImpl implements SearchResult {
    private final static Logger logger = Logger.getLogger(SearchResultImpl.class.getName());

    public static SearchResultImpl runQuery(String queryInput, Query query, SearcherManager searcherManager) throws IOException {
        final IndexSearcher searcher = searcherManager.acquire();
        try {
            final TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
            final Set<Long> uidSet = new HashSet<>();
            final Map<Long, Integer> documentIdMap = new HashMap<>();
            populate(searcher, topDocs, uidSet, documentIdMap);
            return new SearchResultImpl(queryInput, query, searcher, searcherManager, documentIdMap, uidSet);
        } catch (Exception e) {
           searcherManager.release(searcher);
           throw e;
        }
    }

    private static void populate(IndexSearcher searcher, TopDocs topDocs, Set<Long> uidSet, Map<Long, Integer> documentIdMap) throws IOException {
        for(ScoreDoc sd: topDocs.scoreDocs) {
            Document document = searcher.doc(sd.doc);
            Long uid = document.getField("uid").numericValue().longValue();
            uidSet.add(uid);
            documentIdMap.put(uid, sd.doc);
        }
    }

    private final static ResultHighlighter highlighter = new ResultHighlighter();
    private final static HighlightedStringImpl[] EMPTY_HIGHLIGHTS = new HighlightedStringImpl[] { HighlightedStringImpl.EMPTY, HighlightedStringImpl.EMPTY };

    private final static String[] highlightFields = { "subject", "body" };

    private final String queryText;
    private final Query query;
    private final SearcherManager searcherManager;
    private final IndexSearcher searcher;

    private final Map<Long, Integer> documentIdMap;
    private final Set<Long> uidSet;
    private final Map<Long, HighlightedString[]> highlightMap = new HashMap<>();

    public SearchResultImpl(String queryText, Query query, IndexSearcher searcher, SearcherManager searcherManager, Map<Long, Integer> documentIdMap, Set<Long> uidSet) {
        this.queryText = queryText;
        this.query = query;
        this.searcherManager = searcherManager;
        this.searcher = searcher;
        this.documentIdMap = documentIdMap;
        this.uidSet = uidSet;
    }

    public String getQueryText() {
        return queryText;
    }

    public int getMatchCount() {
        return uidSet.size();
    }

    public boolean resultContainsUID(long uid) {
        return uidSet.contains(uid);
    }

    public HighlightedString getHighlightedSubject(long uid) {
        return getHighlightsByUID(uid)[0];
    }

    public HighlightedString getHighlightedBody(long uid) {
        return getHighlightsByUID(uid)[1];
    }

    @Override
    public void dispose() {
        try {
            searcherManager.release(searcher);
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException releasing searcher: "+ e, e);
        }
    }

    private HighlightedString[] getHighlightsByUID(Long uid) {
        synchronized (highlightMap) {
            if (!highlightMap.containsKey(uid)) {
                highlightMap.put(uid, generateHightlight(uid));
            }
            return highlightMap.get(uid);
        }
    }

    private HighlightedString[] generateHightlight(Long uid) {
        if(!documentIdMap.containsKey(uid)) {
            logger.warning("No document id found for UID = "+ uid);
            return EMPTY_HIGHLIGHTS;
        }
        final int docid = documentIdMap.get(uid);

        try {
            final Map<String, String[]> highlights = highlighter.highlightFields(highlightFields, query, searcher, new int[]{docid}, new int[]{2, 2});
            final HighlightedStringImpl[] result = new HighlightedStringImpl[2];
            result[0] = getHighlightedString(highlights, "subject");
            result[1] = getHighlightedString(highlights, "body");
            return result;
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException highlighting result "+ e, e);
            return EMPTY_HIGHLIGHTS;
        }
    }

    private static HighlightedStringImpl getHighlightedString(Map<String, String[]> highlights, String key) {
        final String tagged = highlights.get(key)[0];
        return HighlightedStringImpl.createFromTaggedString(tagged);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for(Long id: uidSet) {
            sb.append(id);
            sb.append(") ");
            sb.append(getHighlightedBody(id));
            sb.append("\n");
        }
        return sb.toString();
    }
}
