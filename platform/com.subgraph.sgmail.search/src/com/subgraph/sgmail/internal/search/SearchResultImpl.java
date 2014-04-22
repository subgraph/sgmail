package com.subgraph.sgmail.internal.search;

import com.subgraph.sgmail.search.HighlightedString;
import com.subgraph.sgmail.search.SearchResult;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class SearchResultImpl implements SearchResult {
    private final static Logger logger = Logger.getLogger(SearchResultImpl.class.getName());

    public static SearchResultImpl runQuery(String queryInput, Query query, SearcherManager searcherManager) throws IOException {
        final IndexSearcher searcher = searcherManager.acquire();
        try {
            final TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);
            final Map<Integer, Integer> documentIdMap = new HashMap<>();
            final boolean[] bitset = populate(searcher, topDocs, documentIdMap);
            return new SearchResultImpl(queryInput, query, searcher, searcherManager, documentIdMap, bitset);
        } catch (Exception e) {
           searcherManager.release(searcher);
           throw e;
        }
    }

    private static boolean[] populate(IndexSearcher searcher, TopDocs topDocs, Map<Integer, Integer> documentIdMap) throws IOException {
        int highest = 0;
        for(ScoreDoc sd: topDocs.scoreDocs) {
            Document document = searcher.doc(sd.doc);
            int uid = document.getField("uid").numericValue().intValue();
            if(uid > highest) {
                highest = uid;
            }
            documentIdMap.put(uid, sd.doc);
        }
        boolean[] bitset = new boolean[highest + 1];
        for(Integer n: documentIdMap.keySet()) {
            bitset[n] = true;
        }
        return bitset;
    }

    private final static ResultHighlighter highlighter = new ResultHighlighter();
    private final static HighlightedStringImpl[] EMPTY_HIGHLIGHTS = new HighlightedStringImpl[] { HighlightedStringImpl.EMPTY, HighlightedStringImpl.EMPTY };

    private final static String[] highlightFields = { "subject", "body" };

    private final String queryText;
    private final Query query;
    private final SearcherManager searcherManager;
    private IndexSearcher searcher;

    private final Map<Integer, Integer> documentIdMap;
    private final boolean[] bitset;
    private final Map<Integer, HighlightedString[]> highlightMap = new HashMap<>();

    public SearchResultImpl(String queryText, Query query, IndexSearcher searcher, SearcherManager searcherManager, Map<Integer, Integer> documentIdMap, boolean[] bitset) {
        this.queryText = queryText;
        this.query = query;
        this.searcherManager = searcherManager;
        this.searcher = searcher;
        this.documentIdMap = documentIdMap;
        this.bitset = bitset;
    }

    public String getQueryText() {
        return queryText;
    }

    public int getMatchCount() {
        return bitset.length;
    }

    public boolean resultContainsMessageId(int uid) {
        if(uid >= bitset.length) {
            return false;
        }
        return bitset[uid];
    }

    public HighlightedString getHighlightedSubject(int uid) {
        return getHighlightsByUID(uid)[0];
    }

    public HighlightedString getHighlightedBody(int uid) {
        return getHighlightsByUID(uid)[1];
    }

    @Override
    public void dispose() {
        try {
            searcherManager.release(searcher);
            searcher = null;
        } catch (IOException e) {
            logger.log(Level.WARNING, "IOException releasing searcher: " + e, e);
        }
    }

    private HighlightedString[] getHighlightsByUID(int uid) {
        synchronized (highlightMap) {
            if (!highlightMap.containsKey(uid)) {
                highlightMap.put(uid, generateHightlight(uid));
            }
            return highlightMap.get(uid);
        }
    }

    private HighlightedString[] generateHightlight(int uid) {
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
}
