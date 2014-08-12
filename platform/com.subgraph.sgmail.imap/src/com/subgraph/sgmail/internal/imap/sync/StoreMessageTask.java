package com.subgraph.sgmail.internal.imap.sync;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;

import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.imap.LocalIMAPFolder;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.nyms.NymsAgent;
import com.subgraph.sgmail.nyms.NymsAgentException;
import com.subgraph.sgmail.nyms.NymsIncomingProcessingResult;
import com.subgraph.sgmail.nyms.NymsIncomingProcessingResult.DecryptionResult;
import com.subgraph.sgmail.search.MessageSearchIndex;

public class StoreMessageTask implements Runnable {
    private final static Logger logger = Logger.getLogger(StoreMessageTask.class.getName());
    private final StoredMessage message;
    private final long messageUID;
    private final MessageSearchIndex searchIndex;
    private final JavamailUtils javamailUtils;
    private final NymsAgent nymsAgent;
    private final boolean alreadyStored;
    private final LocalIMAPFolder folder;

    StoreMessageTask(StoredMessage message, long messageUID, LocalIMAPFolder folder, MessageSearchIndex searchIndex, JavamailUtils javamailUtils, NymsAgent nymsAgent, boolean alreadyStored) {
        this.message = message;
        this.messageUID = messageUID;
        this.folder = folder;
        this.searchIndex = searchIndex;
        this.javamailUtils = javamailUtils;
        this.nymsAgent = nymsAgent;
        this.alreadyStored = alreadyStored;
    }

    @Override
    public void run() {
        try {
            storeMessage();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Unexpected exception in StoreMessageTask: "+ e, e);
        }
    }

    private void storeMessage() {
      if(alreadyStored) {
        folder.appendMessage(message, messageUID);
        return;
      }
      if(message.needsDecryption() || message.isSigned()) {
        nymsAgentProcessing();
      }
      folder.appendMessage(message, messageUID);
      indexMessage();
    }
    

    
    private void nymsAgentProcessing() {
      if(!nymsAgent.getStatus().isAvailable()) {
        return;
      }
      try {
        NymsIncomingProcessingResult result = nymsAgent.processIncomingMessage(message.toMimeMessage(javamailUtils.getSessionInstance()));
        if(result.getDecryptionResult() == DecryptionResult.DECRYPTION_SUCCESS) {
          final String body = javamailUtils.getTextBody(result.getDecryptedMessage());
          final List<MessageAttachment> attachements = javamailUtils.getAttachments(result.getDecryptedMessage());
          message.setDecryptedMessageDetails(result.getRawDecryptedMessage(), body, attachements);
          message.addFlag(StoredMessage.FLAG_DECRYPTED);
        }
        switch(result.getSignatureVerificationResult()) {
        case NOT_SIGNED:
          message.setSignatureStatus(StoredMessage.SIGNATURE_UNSIGNED);
          return;
        case NO_VERIFY_PUBKEY:
          message.setSignatureStatus(StoredMessage.SIGNATURE_NO_PUBKEY);
          break;
        case SIGNATURE_KEY_EXPIRED:
          message.setSignatureStatus(StoredMessage.SIGNATURE_KEY_EXPIRED);
          break;
        case SIGNATURE_INVALID:
          message.setSignatureStatus(StoredMessage.SIGNATURE_INVALID);
          break;
        case SIGNATURE_VALID:
          message.setSignatureStatus(StoredMessage.SIGNATURE_VALID);
          break;
        case VERIFY_FAILED:
          logger.warning("VERIFY_FAILED processing signature: "+ result.getFailureMessage());
          return;
        default:
          logger.warning("Unexpected signature verification code: "+ result.getSignatureVerificationResult());
          return;
        }
        final String keyid = result.getSignatureKeyId();
        if(keyid != null && !keyid.isEmpty()) {
          message.setSignatureKeyId(keyid);
        }
      } catch (NymsAgentException e) {
        logger.warning("Error from nyms agent while attempting decryption: "+ e.getMessage());
      } catch (MessagingException e) {
        logger.warning("Error parsing message into MimeMessage: "+ e);
      } catch (IOException e) {
        logger.warning("I/O Error extracting attachments from decrypted message: "+ e.getMessage());
      }
    }
    
    private void indexMessage() {
      if(message.needsDecryption()) {
        return;
      }
      try {
       searchIndex.addMessage(message); 
      } catch(IOException e) {
        logger.log(Level.WARNING, "IOException indexing message: "+ e, e);
      }
    }
}
