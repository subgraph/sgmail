package com.subgraph.sgmail.accounts.impl;

import com.db4o.ObjectContainer;
import com.subgraph.sgmail.testutils.Db4oUtils;
import com.subgraph.sgmail.testutils.db4oevents.Db4oEventTracker;
import com.subgraph.sgmail.testutils.db4oevents.Event;
import org.junit.Before;

public class AccountActivationTest {

    private ObjectContainer db;
    private Db4oEventTracker tracker;

    @Before
    public void setup() {
        db = Db4oUtils.openMemoryDatabase();
        tracker = new Db4oEventTracker(db);
        tracker.setEventFilterAllExcept(Event.EventType.INSTANTIATE, Event.EventType.COMMIT);
    }

    /*
    private IMAPAccount createTestAccount() {
        final SMTPAccount smtpAccount = Accounts.createSMTPAccount("smtp.example.com", 2525, "smtpLogin", "smtpPassword");
        final IMAPAccountImpl.Builder builder = new IMAPAccountImpl.Builder();
        return builder.smtpAccount(smtpAccount)
                .emailAddress("user@example.com")
                .label("Example Account")
                .hostname("imap.example.com")
                .port(123)
                .login("imapLogin")
                .password("imapPassword")
                .build();
    }
   /

    private void storeTestAccount() {
        db.store(createTestAccount());
        db.commit();
        db.ext().purge();
    }

    private IMAPAccount lookupAccount() {
        return lookupAccount(true, true);
    }

    private IMAPAccount lookupAccount(boolean storeFirst, boolean clearTracker) {
        if(storeFirst) {
            storeTestAccount();
        }

        final ObjectSet<IMAPAccount> result = db.query(IMAPAccount.class);
        if(result.size() != 1) {
            throw new IllegalStateException("Expecting a single IMAPAccount result");
        }
        final IMAPAccount imapAccount = result.get(0);
        if(clearTracker) {
            tracker.clearEvents();
        }
        return imapAccount;
    }


    @Test
    public void testCreateIMAPAccount() {
        storeTestAccount();
        tracker.assertCREATE("ActivatableArrayList", "ActivatableHashMap", "ActivatableHashMap", "StoredAccountPreferences",
                "PasswordAuthenticationCredentialsImpl", "SMTPAccountImpl", "ActivatableArrayList", "StoredMessageLabelCollectionImpl",
                "ActivatableArrayList", "PasswordAuthenticationCredentialsImpl", "IMAPAccountImpl");
    }

    @Test
    public void testActivateIMAPAccount() {
        IMAPAccount imapAccount = lookupAccount();
        imapAccount.isGmailAccount();
        tracker.assertACTIVATE("IMAPAccountImpl");
    }
    */

}
