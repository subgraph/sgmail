package com.subgraph.sgmail.search.impl;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

class MessageIndexReader {

    private final IndexWriter indexWriter;
    private IndexReader reader;

    MessageIndexReader(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
    }

    IndexSearcher getIndexSearcher() throws IOException {
        return new IndexSearcher(getReader());
    }

    IndexReader getReader() throws IOException {
        if(reader == null) {
            reader = DirectoryReader.open(indexWriter, false);
        }
        return reader;
    }

    void closeIndex() {
        if(reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
