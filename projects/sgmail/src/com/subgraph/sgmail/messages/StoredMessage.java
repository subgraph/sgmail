package com.subgraph.sgmail.messages;

import com.subgraph.sgmail.messages.impl.StoredMessageBuilder;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.List;

/**
 * A message we have stored locally.  The direct members of this class are the minimum set of information needed to sort
 * messages into conversations and to chronologically sort the messages within each conversation.  Further information
 * about each message can be lazily accessed as needed through the <code>StoredMessageSummary</code> field.
 */
public interface StoredMessage {

    public final static int FLAG_ANSWERED           = 0x01;
    public final static int FLAG_DELETED            = 0x02;
    public final static int FLAG_DRAFT              = 0x04;
    public final static int FLAG_FLAGGED            = 0x08;
    public final static int FLAG_RECENT             = 0x10;
    public final static int FLAG_SEEN               = 0x20;
    public final static int FLAG_ENCRYPTED          = 0x1000;

    int getMessageId();

    /**
     * Returns the unique identity value for the conversation this message is a member of.
     *
     * @return the conversation identity value for the conversation this message is a member of.
     */
    int getConversationId();

    /**
     * Returns the time this message was received represented as the number of seconds since January 1, 1970, 00:00:00 GMT.
     *
     * @return the time this message was received represented as the number of seconds since January 1, 1970, 00:00:00 GMT.
     */
    int getMessageDate();

    void addLabel(StoredMessageLabel label);
    void removeLabel(StoredMessageLabel label);
    boolean containsLabel(StoredMessageLabel label);

    String getSubject();
    String getBodySnippet();
    String getBodyText();
    List<MessageAttachment> getAttachments();
    MessageUser getSender();
    List<MessageUser> getToRecipients();
    List<MessageUser> getCCRecipients();

    boolean isFlagSet(int flag);
    int getFlags();
    void setFlags(int value);
    void addFlag(int flag);
    void removeFlag(int flag);

    byte[] getRawMessageBytes();
    InputStream getRawMessageStream();
    MimeMessage toMimeMessage() throws MessagingException;

    void purgeContent();
    int incrementReferenceCount();
    int decrementReferenceCount();
    int getReferenceCount();

     interface Builder {
        static Builder create(byte[] rawBytes) {
            return new StoredMessageBuilder(rawBytes);
        }
        Builder subject(String value);
        Builder bodySnippet(String value);
        Builder bodyText(String value);
        Builder messageId(int value);
        Builder conversationId(int value);
        Builder messageDate(int value);
        Builder flags(int value);
        Builder sender(MessageUser value);
        Builder toRecipients(List<MessageUser> value);
        Builder ccRecipients(List<MessageUser> value);
        Builder attachments(List<MessageAttachment> value);
        Builder labels(List<StoredMessageLabel> value);
        StoredMessage build();
    }
}
