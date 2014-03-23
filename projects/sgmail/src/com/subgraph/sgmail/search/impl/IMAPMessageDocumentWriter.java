package com.subgraph.sgmail.search.impl;

import com.subgraph.sgmail.messages.StoredMessage;
import org.apache.lucene.document.*;
import org.apache.lucene.index.FieldInfo;
import org.apache.lucene.index.IndexWriter;

import java.io.IOException;
import java.util.Objects;

class IMAPMessageDocumentWriter {

    private final Field uidField;
    private final Field subjectField;
    private final Field bodyField;
    private final Document document;

    IMAPMessageDocumentWriter() {
        this.document = new Document();
        final FieldType offsetsType = createOffsetsType();
        this.uidField = new LongField("uid", 0, Field.Store.YES);
        this.subjectField = new Field("subject", "", offsetsType);
        this.bodyField = new Field("body", "", offsetsType);
        document.add(uidField);
        document.add(subjectField);
        document.add(bodyField);
    }

    private FieldType createOffsetsType() {
        final FieldType offsetsType = new FieldType(TextField.TYPE_STORED);
        offsetsType.setIndexOptions(FieldInfo.IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        return offsetsType;
    }

    synchronized void indexMessage(IndexWriter writer, StoredMessage message) throws IOException {
        uidField.setLongValue(message.getUniqueMessageId());
        subjectField.setStringValue(Objects.requireNonNull(message.getSubject()));
        bodyField.setStringValue(Objects.requireNonNull(message.getDisplayText()));
        System.out.println("Indexing message "+ Long.toUnsignedString(message.getUniqueMessageId()));
        writer.addDocument(document);
    }
}
