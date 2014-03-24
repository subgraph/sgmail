package com.subgraph.sgmail.messages.impl;

import com.db4o.activation.ActivationPurpose;
import com.subgraph.sgmail.messages.StoredFolder;
import com.subgraph.sgmail.messages.StoredIMAPFolder;
import com.subgraph.sgmail.messages.StoredIMAPMessage;
import com.subgraph.sgmail.model.LocalMimeMessage;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;

public class StoredIMAPMessageImpl extends StoredMessageImpl implements StoredIMAPMessage {


    private final StoredIMAPMessageSummary imapSummary;

    private transient MimeMessage cachedMimeMessage;

    public StoredIMAPMessageImpl(long conversationId, long messageDate, StoredIMAPMessageSummary summary) {
        super(conversationId, messageDate, summary);
        this.imapSummary = summary;
    }

    @Override
    public StoredIMAPFolder getIMAPFolder() {
        return downcastFolder(getFolder());
    }

    private StoredIMAPFolder downcastFolder(StoredFolder folder) {
        if(folder == null) {
            return null;
        } else  if(!(folder instanceof StoredIMAPFolder)) {
           throw new IllegalStateException("Folder set for StoredIMAPMessage is not expected StoredIMAPFolder "+ folder);
       } else {
            return (StoredIMAPFolder) folder;
        }
    }

    @Override
    public long getMessageUID() {
        activate(ActivationPurpose.READ);
        return imapSummary.getMessageUID();
    }

    @Override
    public int getMessageNumber() {
        activate(ActivationPurpose.READ);
        return imapSummary.getMessageNumber();
    }

    @Override
    public void setMessageNumber(int value) {
        activate(ActivationPurpose.READ);
        imapSummary.setMessageNumber(value);
    }

    @Override
    public synchronized MimeMessage toMimeMessage() throws MessagingException {
        activate(ActivationPurpose.READ);
        if(cachedMimeMessage == null) {
            cachedMimeMessage = new LocalMimeMessage(this, model.getSession(), new ByteArrayInputStream(imapSummary.getRawMessageBytes()));
        }
        return cachedMimeMessage;
    }


}
