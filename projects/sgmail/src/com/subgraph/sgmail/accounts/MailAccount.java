package com.subgraph.sgmail.accounts;

import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

public interface MailAccount extends Account {
    SMTPAccount getSMTPAccount();
    Store getRemoteStore();
    String getEmailAddress();
    String getDomain();
    String getRealname();
    AuthenticationCredentials getAuthenticationCredentials();
    String getHostname();
    String getOnionHostname();
    int getPort();
    URLName getURLName();
    long generateConversationIdForMessage(MimeMessage message) throws MessagingException;
    long generateUniqueMessageIdForMessage(MimeMessage message) throws MessagingException;
}
