package com.subgraph.sgmail.directory.client;

import com.google.common.primitives.UnsignedLongs;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveRegistrationEmailTask implements Callable<MimeMessage> {
    private final static Logger logger = Logger.getLogger(ReceiveRegistrationEmailTask.class.getName());

    private final static long CHECK_BACK_MESSAGES_PERIOD = TimeUnit.MINUTES.toMillis(5);

    private final String imapLogin;
    private final String imapPassword;
    private final String imapServer;
    private long expectedRequestId;


    private MimeMessage messageReceived;
    private IMAPFolder idleFolder;

    public ReceiveRegistrationEmailTask(String imapServer, String imapLogin, String imapPassword) {
        this.imapServer = imapServer;
        this.imapLogin = imapLogin;
        this.imapPassword = imapPassword;
        this.expectedRequestId = 0;
    }

    public synchronized void setExpectedRequestId(long requestId) {
        logger.fine("Expected request id set to "+ requestId);
        this.expectedRequestId = requestId;
        notifyAll();
    }

    @Override
    public MimeMessage call() throws Exception {
        try {
            return runRegistrationTask();
        } catch(Exception e) {
            logger.log(Level.WARNING, "Unhandled exception in ReceiveRegistrationTask", e);
            throw e;
        }
    }

    private MimeMessage runRegistrationTask() throws Exception {
        logger.fine("Starting ReceiveRegistrationEmailTask");
        final IMAPStore store = openStore();
        try {
            final IMAPFolder inbox = openInbox(store);
            final MimeMessage msg = searchFolder(inbox);

            if(msg != null) {
                if(inbox.isOpen()) inbox.close(false);
                return msg;
            }
            return waitForMessage(inbox);
        } finally {
            store.close();
        }
    }



    private MimeMessage waitForMessage(IMAPFolder inbox) throws MessagingException {
        idleFolder = inbox;
        while(messageReceived == null) {
            inbox.idle(true);
        }
        if(inbox.isOpen()) {
            inbox.close(false);
        }
        return messageReceived;
    }

    private IMAPStore openStore() throws MessagingException {
        final Session session = Session.getInstance(new Properties());
        session.setDebug(true);
        final IMAPStore store = (IMAPStore) session.getStore("imaps");
        store.connect(imapServer, imapLogin, imapPassword);
        return store;
    }

    private IMAPFolder openInbox(IMAPStore store) throws MessagingException {
        final IMAPFolder inbox = (IMAPFolder) store.getFolder("INBOX");
        inbox.open(IMAPFolder.READ_WRITE);
        inbox.addMessageCountListener(createMessageCountListener());
        return inbox;
    }

    private MimeMessage searchFolder(IMAPFolder folder) throws MessagingException {
        final int count = folder.getMessageCount();
        final long now = new Date().getTime();
        int idx = count;
        while(idx > 0) {
            Message m = folder.getMessage(idx);
            if(now - m.getReceivedDate().getTime() > CHECK_BACK_MESSAGES_PERIOD) {
                return null;
            }
            if(messageMatchesRequestId(m)) {
                return (MimeMessage) m;
            }
            idx -= 1;
        }
        return null;
    }

    private MessageCountListener createMessageCountListener() {
        return new MessageCountAdapter() {
            @Override
            public void messagesAdded(MessageCountEvent messageCountEvent) {
                for(Message m: messageCountEvent.getMessages()) {
                    processIncomingMessage(m);
                }
            }
        };
    }

    private void processIncomingMessage(Message message) {
        logger.fine("processIncomingMessage()");
        if(messageMatchesRequestId(message)) {
            logger.fine("Message matches expected request id");
            messageReceived = (MimeMessage) message;
            if(idleFolder != null) {
                try {
                    idleFolder.close(false);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean messageMatchesRequestId(Message message)  {
        try {
            final String[] headers = message.getHeader("X-SGMAIL-IDENTITY-REGISTRATION");
            if(headers == null || headers.length != 1) {
                return false;
            }
            return headerMatchesRequestId(headers[0]);
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }

    }
    private boolean headerMatchesRequestId(String header) {
        final String[] parts = header.split(":");
        if(parts.length != 2) {
            return false;
        }
        synchronized (this) {
            while(expectedRequestId == 0) {
                try {
                    wait();
                } catch (InterruptedException e) {

                    e.printStackTrace();
                    return false;
                }
            }
        }
        return UnsignedLongs.parseUnsignedLong(parts[0], 16) == expectedRequestId;
    }
}
