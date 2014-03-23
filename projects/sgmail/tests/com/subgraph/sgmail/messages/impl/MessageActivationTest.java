package com.subgraph.sgmail.messages.impl;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.subgraph.sgmail.messages.StoredIMAPMessage;
import com.subgraph.sgmail.messages.StoredMessages;
import com.subgraph.sgmail.testutils.Db4oUtils;
import com.subgraph.sgmail.testutils.JavamailUtils;
import com.subgraph.sgmail.testutils.db4oevents.Db4oEventTracker;
import com.subgraph.sgmail.testutils.db4oevents.Event;
import com.subgraph.sgmail.testutils.db4oevents.ExpectedEvents;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageActivationTest {

    private enum ClassSet {
        MESSAGE_BASE    ("HashSet", "StoredIMAPMessageImpl"),
        MESSAGE_SUMMARY ("ArrayList", "ArrayList", "StoredIMAPMessageSummary"),
        RAW_MESSAGE     ("StoredMessageRawData");

        private List<String> classNames;

        ClassSet(String... names) {
            classNames = Arrays.asList(names);
        }

        List<String> getClassNames() {
            return classNames;
        }

    }

    private ObjectContainer db;
    private Db4oEventTracker tracker;

    @Before
    public void setup() {
        db = Db4oUtils.openMemoryDatabase();
        tracker = new Db4oEventTracker(db);
        tracker.setEventFilterAllExcept(Event.EventType.INSTANTIATE, Event.EventType.COMMIT);
    }

    @After
    public void cleanup() {
        db.close();
    }

    private String[] getClassNames(ClassSet... classSets) {
        final List<String> names = new ArrayList<>();
        for(ClassSet cs: classSets) {
            names.addAll(cs.getClassNames());
        }
        return names.toArray(new String[names.size()]);
    }

    private static StoredIMAPMessage createTestStoredMessage() {
        final MimeMessage mimeMessage = JavamailUtils.createTestMimeMessage();
        try {
            return StoredMessages.createIMAPMessage(mimeMessage, 13, 23, 33);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unexpected exception creating test message: "+ e, e);
        }
    }

    private void storeTestMessage() {
        storeTestMessage(true);
    }

    private void storeTestMessage(boolean clearTracker) {
        db.store(createTestStoredMessage());
        db.commit();
        db.ext().purge();
        if(clearTracker) {
            tracker.clearEvents();
        }
    }

    @Test
    public void testStoredMessageStore() throws MessagingException {
        storeTestMessage(false);
        tracker.assertCREATE(getClassNames(ClassSet.RAW_MESSAGE, ClassSet.MESSAGE_SUMMARY, ClassSet.MESSAGE_BASE));
    }

    @Test
    public void testActivateBaseStoredMessage() {
        final StoredIMAPMessage message = lookupMessage();
        message.getMessageDate();
        tracker.assertACTIVATE(getClassNames(ClassSet.MESSAGE_BASE));
    }

    @Test
    public void testActivateMessageSummary() {
        final StoredIMAPMessage message = lookupMessage();
        message.getMessageUID();
        tracker.assertACTIVATE(getClassNames(ClassSet.MESSAGE_BASE, ClassSet.MESSAGE_SUMMARY));
    }

    @Test
    public void testUpdateBaseMessage() {
        final ExpectedEvents expected = ExpectedEvents
                .expect(Event.EventType.ACTIVATE, "HashSet", "StoredIMAPMessageImpl")
                .updates("StoredIMAPMessageImpl");
        final StoredIMAPMessage message = lookupMessage();
        message.setFlags(0);
        db.commit();
        expected.assertExpectation(tracker.getEventList());
    }

    @Test
    public void testUpdateMessageSummary() {
        final StoredIMAPMessage message = lookupMessage();
        message.setMessageNumber(0);
        tracker.assertACTIVATE(getClassNames(ClassSet.MESSAGE_BASE, ClassSet.MESSAGE_SUMMARY));
        db.commit();
        tracker.assertUPDATE("StoredIMAPMessageSummary");
    }

    private StoredIMAPMessage lookupMessage() {
       return lookupMessage(true, true);
    }

    private StoredIMAPMessage lookupMessage(boolean storeFirst, boolean clearTracker) {
        if(storeFirst) {
            storeTestMessage();
        }
        final ObjectSet<StoredIMAPMessage> result = db.query(StoredIMAPMessage.class);
        if(result.size() != 1) {
           throw new IllegalStateException("Expecting a single StoredIMAPMessage result and got "+ result.size());
        }
        final StoredIMAPMessage message = result.get(0);
        if(clearTracker) {
            tracker.clearEvents();
        }
        return message;
    }
}
