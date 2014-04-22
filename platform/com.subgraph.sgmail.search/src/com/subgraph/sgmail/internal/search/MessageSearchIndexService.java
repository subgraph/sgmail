package com.subgraph.sgmail.internal.search;

import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.search.MessageSearchIndex;
import com.subgraph.sgmail.search.SearchResult;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageSearchIndexService implements MessageSearchIndex {
    private final static Logger logger = Logger.getLogger(MessageSearchIndexService.class.getName());

    private File indexDirectory;
    private final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);

    private MessageIndexWriter writer;
    private SearcherManager searcherManager;
    private boolean isClosed;

    public void setIndexDirectory(File indexDirectory) {
    	this.indexDirectory = indexDirectory;
    }
    
    public void addMessage(StoredMessage message) throws IOException {
        getWriter().indexMessage(message);
    }

    public void removeMessage(StoredMessage message) {
        logger.warning("removeMessage called but not yet implemented");
    }

    public SearchResult search(String queryString) throws IOException {
        getSearcherManager().maybeRefresh();
        final Query query = createQuery(queryString);
        return SearchResultImpl.runQuery(queryString, query, getSearcherManager());
    }

    private Query createQuery(String input) {
        try {
            return createQueryParser().parse(input);
        } catch (ParseException e) {
            final BooleanQuery query = new BooleanQuery();
            query.add(new TermQuery(new Term("body", input.toLowerCase())), BooleanClause.Occur.SHOULD);
            query.add(new TermQuery(new Term("subject", input.toLowerCase())), BooleanClause.Occur.SHOULD);
            return query;
        }
    }

    private QueryParser createQueryParser() {
        return new MultiFieldQueryParser(Version.LUCENE_47, new String[] { "body", "subject" }, analyzer);
    }

    public void commit() {
        try {
            getWriter().commitIndex();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error committing search index: "+ e.getMessage(), e);
        }
    }

    public synchronized void close() {
        if(isClosed) {
            return;
        } else {
            isClosed = true;
        }
        if(writer != null) {
            writer.closeIndex();
        }
        if(searcherManager != null) {
            try {
                searcherManager.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "IOException closing SearchManager: " + e, e);
            }
        }
    }

    private synchronized MessageIndexWriter getWriter() throws IOException {
        if(isClosed) {
            throw new IOException("Index has been closed");
        }
        if(indexDirectory == null) {
        	logger.warning("indexDirectory has not been set in MessageSearchIndex.getWriter()");
        }
        if(writer == null) {
            writer = MessageIndexWriter.openIndexWriter(indexDirectory, false);
        }
        return writer;
    }

    private synchronized SearcherManager getSearcherManager() throws IOException {
        if(isClosed) {
            throw new IOException("Index has been closed");
        }
        if(searcherManager == null) {
            searcherManager = new SearcherManager(getWriter().getIndexWriter(), false, null);
        }
        return searcherManager;
    }
}
