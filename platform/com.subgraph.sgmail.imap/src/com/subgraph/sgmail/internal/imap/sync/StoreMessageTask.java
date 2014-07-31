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
    private final LocalIMAPFolder folder;

    StoreMessageTask(StoredMessage message, long messageUID, LocalIMAPFolder folder, MessageSearchIndex searchIndex, JavamailUtils javamailUtils, NymsAgent nymsAgent) {
        this.message = message;
        this.messageUID = messageUID;
        this.folder = folder;
        this.searchIndex = searchIndex;
        this.javamailUtils = javamailUtils;
        this.nymsAgent = nymsAgent;
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
      if(message.needsDecryption() && attemptDecrypt()) {
        message.addFlag(StoredMessage.FLAG_DECRYPTED);
      }
      folder.appendMessage(message, messageUID);
      indexMessage();
    }
    
    private boolean attemptDecrypt() {
      if(!nymsAgent.getStatus().isAvailable()) {
        return false;
      }
      try {
        NymsIncomingProcessingResult result = nymsAgent.processIncomingMessage(message.toMimeMessage(javamailUtils.getSessionInstance()));
        if(result.getDecryptionResult() == DecryptionResult.DECRYPTION_SUCCESS) {
          final String body = javamailUtils.getTextBody(result.getDecryptedMessage());
          final List<MessageAttachment> attachements = javamailUtils.getAttachments(result.getDecryptedMessage());
          message.setDecryptedMessageDetails(result.getRawDecryptedMessage(), body, attachements);
          return true;
        }
        switch(result.getSignatureVerificationResult()) {
        case NOT_SIGNED:
          break;
        case NO_VERIFY_PUBKEY:
          message.setSignatureStatus(StoredMessage.SIGNATURE_NO_PUBKEY);
          break;
        case SIGNATURE_INVALID:
          message.setSignatureStatus(StoredMessage.SIGNATURE_INVALID);
          break;
        case SIGNATURE_VALID:
          message.setSignatureStatus(StoredMessage.SIGNATURE_VALID);
          break;
        default:
          break;
        
        }
      } catch (NymsAgentException e) {
        logger.warning("Error from nyms agent while attempting decryption: "+ e.getMessage());
      } catch (MessagingException e) {
        logger.warning("Error parsing message into MimeMessage: "+ e);
      } catch (IOException e) {
        logger.warning("I/O Error extracting attachments from decrypted message: "+ e.getMessage());
      }
      return false;
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
