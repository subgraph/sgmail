package com.subgraph.sgmail.messages;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * A message we have stored locally.  The direct members of this class are the minimum set of information needed to sort
 * messages into conversations and to chronologically sort the messages within each conversation.  Further information
 * about each message can be lazily accessed as needed through the <code>StoredMessageSummary</code> field.
 */
public interface StoredMessage {

    public final static long FLAG_ANSWERED           = 0x01;
    public final static long FLAG_DELETED            = 0x02;
    public final static long FLAG_DRAFT              = 0x04;
    public final static long FLAG_FLAGGED            = 0x08;
    public final static long FLAG_RECENT             = 0x10;
    public final static long FLAG_SEEN               = 0x20;

    long getUniqueMessageId();

    /**
     * Returns the unique identity value for the conversation this message is a member of.
     *
     * @return the conversation identity value for the conversation this message is a member of.
     */
    long getConversationId();

    /**
     * Returns the time this message was received represented as the number of milliseconds since January 1, 1970, 00:00:00 GMT.
     *
     * @return the time this message was received represented as the number of milliseconds since January 1, 1970, 00:00:00 GMT.
     */
    long getMessageDate();

    StoredFolder getFolder();
    void setFolder(StoredFolder folder);

    void addLabel(StoredMessageLabel label);
    void removeLabel(StoredMessageLabel label);
    Set<StoredMessageLabel> getLabels();

    String getSubject();
    String getDisplayText();
    List<MessageAttachment> getAttachments();
    MessageUser getSender();
    List<MessageUser> getRecipients();

    boolean isFlagSet(long flag);
    long getFlags();
    void setFlags(long value);
    void addFlag(long flag);
    void removeFlag(long flag);

    byte[] getRawMessageBytes();
    InputStream getRawMessageStream();
}
