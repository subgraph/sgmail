package com.subgraph.sgmail.internal.messages;

import com.subgraph.sgmail.messages.*;

import java.util.List;

public class StoredMessageBuilder implements StoredMessage.Builder {
    byte[] rawDataBytes;
    String subject;
    String bodySnippet;
    String bodyText;
    int messageId;
    int conversationId;
    int messageDate;
    int flags;
    MessageUser sender;
    List<MessageUser> toRecipients;
    List<MessageUser> ccRecipients;
    List<MessageAttachment> messageAttachments;
    List<StoredMessageLabel> messageLabels;

    public StoredMessageBuilder(byte[] rawBytes) {
        this.rawDataBytes = rawBytes;
    }


    @Override
    public StoredMessage.Builder subject(String value) {
        this.subject = value;
        return this;
    }

    @Override
    public StoredMessage.Builder bodySnippet(String value) {
        this.bodySnippet = value;
        return this;
    }

    @Override
    public StoredMessage.Builder bodyText(String value) {
        this.bodyText = value;
        return this;
    }


    @Override
    public StoredMessage.Builder messageId(int value) {
        messageId = value;
        return this;
    }

    @Override
    public StoredMessage.Builder conversationId(int value) {
        conversationId = value;
        return this;
    }

    @Override
    public StoredMessage.Builder messageDate(int value) {
        this.messageDate = value;
        return this;
    }

    @Override
    public StoredMessage.Builder flags(int value) {
        this.flags = value;
        return this;
    }

    @Override
    public StoredMessage.Builder sender(MessageUser value) {
        this.sender = value;
        return this;
    }

    @Override
    public StoredMessage.Builder toRecipients(List<MessageUser> value) {
        this.toRecipients = value;
        return this;
    }

    @Override
    public StoredMessage.Builder ccRecipients(List<MessageUser> value) {
        this.ccRecipients = value;
        return this;
    }

    @Override
    public StoredMessage.Builder attachments(List<MessageAttachment> value) {
        messageAttachments = value;
        return this;
    }

    MessageAttachment[] getAttachmentArray() {
        if(messageAttachments == null || messageAttachments.isEmpty()) {
            return null;
        }
        return messageAttachments.toArray(new MessageAttachment[messageAttachments.size()]);
    }

    MessageUser[] getCCRecipients() {
        return toUserArray(ccRecipients);
    }

    MessageUser[] getToRecipients() {
        return toUserArray(toRecipients);
    }

    private static MessageUser[] toUserArray(List<MessageUser> userList) {
        if(userList == null || userList.isEmpty()) {
            return null;
        }
        return userList.toArray(new MessageUser[userList.size()]);
    }

    @Override
    public StoredMessage.Builder labels(List<StoredMessageLabel> value) {
        messageLabels = value;
        return this;
    }

    @Override
    public StoredMessage build() {
        if(bodySnippet == null && bodyText == null) {
            bodySnippet = bodyText = "";
        } else if(bodySnippet == null) {
        	bodySnippet = createSnippetFromBody(bodyText);
        }

        final StoredMessageContent content = new StoredMessageContent(this);
        final StoredMessageSummary summary = new StoredMessageSummary(this, content);
        final StoredMessage msg = new StoredMessageImpl(this, summary);
        if(messageLabels != null) {
            for (StoredMessageLabel label : messageLabels) {
                msg.addLabel(label);
            }
        }
        return msg;
    }
    
    static String createSnippetFromBody(String body) {
    	if(body == null) {
    		return "";
    	} else if(body.length() <= 1000) {
    		return body;
    	} else {
    		return body.substring(0, 1000);
    	}
    }
}
