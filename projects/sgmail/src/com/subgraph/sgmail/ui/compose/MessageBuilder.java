package com.subgraph.sgmail.ui.compose;

import com.subgraph.sgmail.accounts.IMAPAccount;
import com.subgraph.sgmail.model.Contact;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

public class MessageBuilder {
    private final static Logger logger = Logger.getLogger(MessageBuilder.class.getName());

    final MessageCompositionState state;

    MessageBuilder(MessageCompositionState state) {
        this.state = state;
    }

    public MimeMessage createMessage() throws MessagingException {
        final Session session = Session.getInstance(new Properties());
        final MimeMessage msg = new MimeMessage(session) {
          protected void updateMessageID() throws MessagingException {
              setHeader("Message-ID", generateMessageID(state.getSelectedAccount()));
          }
        };
        addRecipients(msg, Message.RecipientType.TO);
        addRecipients(msg, Message.RecipientType.CC);
        addRecipients(msg, Message.RecipientType.BCC);

        msg.setFrom(getFromAddress());
        msg.setSubject(state.getSubject());

        if(state.getReplyMessage() != null) {
            configureNewMessageForReply(msg, state.getReplyMessage());
        }

        return msg;
    }

    private void configureNewMessageForReply(MimeMessage newMessage, MimeMessage replyMessage) throws MessagingException {
        final String msgId = replyMessage.getMessageID();
        if(msgId != null) {
            newMessage.setHeader("In-Reply-To", msgId);
        }
        final String refs = getReferences(replyMessage, msgId);
        if(refs != null) {
            newMessage.setHeader("References", MimeUtility.fold(12, refs));
        }
    }

    final String getReferences(MimeMessage msg, String messageId) throws MessagingException {
        String refs = msg.getHeader("References", " ");
        if(refs == null) {
            refs = msg.getHeader("In-Reply-To", " ");
        }
        if(messageId != null) {
            if(refs != null) {
                refs = MimeUtility.unfold(refs) + " " + messageId;
            } else {
                refs = messageId;
            }
        }
        return refs;
    }

    private String generateMessageID(IMAPAccount account) {
        final UUID uuid = UUID.randomUUID();
        if(account == null) {
            return "<"+ uuid + ">";
        } else {
            return "<"+ uuid + "@" + account.getDomain() + ">";
        }
    }

    private void addRecipients(MimeMessage message,  Message.RecipientType type)  {
        final List<Contact> contacts = state.getContactsForSection(type);
        if(contacts == null || contacts.isEmpty()) {
            return;
        }
        final InternetAddress[] addresses = new InternetAddress[contacts.size()];
        try {
            for(int i = 0; i < addresses.length; i++) {
                addresses[i] = contacts.get(i).toInternetAddress();
            }
            message.setRecipients(type, addresses);

        } catch (MessagingException|UnsupportedEncodingException e) {
            logger.warning("Exception adding addresses to outgoing message: "+ e);
            return;
        }
    }

    private Address getFromAddress() throws AddressException {
        final IMAPAccount account = state.getSelectedAccount();
        return new InternetAddress(account.getEmailAddress(), true);
    }

}
