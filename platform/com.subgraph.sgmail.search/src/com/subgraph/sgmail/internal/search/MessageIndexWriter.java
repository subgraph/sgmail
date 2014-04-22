package com.subgraph.sgmail.internal.search;

import com.subgraph.sgmail.messages.StoredMessage;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

class MessageIndexWriter {

    private final static Logger logger = Logger.getLogger(MessageIndexWriter.class.getName());

    static MessageIndexWriter openIndexWriter(File indexDirectory, boolean create) throws IOException {
        final Directory dir = FSDirectory.open(indexDirectory);
        final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
        final IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_47, analyzer);
        iwc.setOpenMode((create) ?
                (IndexWriterConfig.OpenMode.CREATE) :
                (IndexWriterConfig.OpenMode.CREATE_OR_APPEND));

        return new MessageIndexWriter(new IndexWriter(dir, iwc));
    }

    private final IndexWriter indexWriter;
    private final IMAPMessageDocumentWriter messageWriter;
    private final Object lock = new Object();
    private boolean isClosed;

    private MessageIndexWriter(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
        this.messageWriter = new IMAPMessageDocumentWriter();
    }

    public IndexWriter getIndexWriter() {
        return indexWriter;
    }

    public void commitIndex() throws IOException {
        indexWriter.commit();
    }

    public void closeIndex() {
        try {
            indexWriter.commit();
            synchronized (lock) {
                isClosed = true;
                indexWriter.close();
            }
        } catch (IOException e) {
            logger.warning("IOException closing search index writer: " + e);
        }
    }

    public void indexMessage(StoredMessage message) {
        try {
            synchronized (lock) {
                if(!isClosed) {
                    messageWriter.indexMessage(indexWriter, message);
                }
            }
        } catch (IOException e) {
            logger.warning("IOException writing to search index: " + e);
        }
    }

    public void removeMessage(StoredMessage message) {
        synchronized (lock) {
            if(!isClosed) {
                //indexWriter.deleteDocuments(new Term("uid", ""));
                //indexWriter.commit();
            }
        }

    }
}
