package com.subgraph.sgmail.ui.panes.right;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.subgraph.sgmail.IEventBus;
import com.subgraph.sgmail.JavamailUtils;
import com.subgraph.sgmail.events.MessageStateChangedEvent;
import com.subgraph.sgmail.messages.MessageAttachment;
import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.nyms.NymsAgent;
import com.subgraph.sgmail.nyms.NymsAgentException;
import com.subgraph.sgmail.nyms.NymsIncomingProcessingResult;
import com.subgraph.sgmail.nyms.NymsKeyInfo;
import com.subgraph.sgmail.search.MessageSearchIndex;
import com.subgraph.sgmail.ui.dialogs.PassphraseDialog;

public class MessageDecryptor {
  private final static Logger logger = Logger.getLogger(MessageDecryptor.class.getName());
  private final Shell shell;
  private final IEventBus eventBus;
  private final JavamailUtils javamailUtils;
  private final MessageSearchIndex searchIndex;
  private final NymsAgent nymsAgent;

  MessageDecryptor(Shell shell, IEventBus eventBus, JavamailUtils javamailUtils, MessageSearchIndex searchIndex, NymsAgent nymsAgent) {
    this.shell = shell;
    this.eventBus = eventBus;
    this.javamailUtils = javamailUtils;
    this.searchIndex = searchIndex;
    this.nymsAgent = nymsAgent;
  }

  void maybeDecryptMessage(StoredMessage message, boolean promptPassphrase) {
    try {
      MimeMessage mimeMessage = message.toMimeMessage(javamailUtils.getSessionInstance());
      NymsIncomingProcessingResult result = nymsAgent.processIncomingMessage(mimeMessage);
      processIncomingMessageResult(message, result, promptPassphrase);
    } catch (MessagingException e) { 
      logger.log(Level.WARNING, "Error converting message to mime message: "+ e.getMessage(), e);
    } catch(NymsAgentException e) {
      logger.warning("Error processing message with nyms agent: "+ e.getMessage());
    }
  }

  private boolean showPassphraseDialog(final List<NymsKeyInfo> keys, StoredMessage message) {
    final boolean[] ok = new boolean[1];
    shell.getDisplay().syncExec(new Runnable() {
      @Override
      public void run() {
        PassphraseDialog dialog = new PassphraseDialog(shell, nymsAgent, keys);
        if (dialog.open() == Window.OK) {
          ok[0] = true;
        }
      }
    });
    return ok[0];
  }

  private void processPassphrase(List<String> keyIds, StoredMessage message) {
    final List<NymsKeyInfo> keys = getKeyInfoForKeyIds(keyIds);
    if(showPassphraseDialog(keys, message)) {
      maybeDecryptMessage(message, false);
    }
    /*
    final NymsIncomingProcessingResult result = showPassphraseDialog(keys, message);
    if (result != null) {
      processIncomingMessageResult(message, result, false);
      return result.getDecryptionResult() == DecryptionResult.DECRYPTION_SUCCESS;
    }
    */
  }

  private List<NymsKeyInfo> getKeyInfoForKeyIds(List<String> keyIds) {
    final List<NymsKeyInfo> keys = new ArrayList<NymsKeyInfo>();
    if (keyIds == null || keyIds.isEmpty()) {
      return keys;
    }
    for (String keyId : keyIds) {
      NymsKeyInfo keyInfo = getSecretKeyInfoById(keyId);
      if(keyInfo != null) {
        keys.add(keyInfo);
      }
    }
    return keys;
  }
  
  private NymsKeyInfo getSecretKeyInfoById(String keyId) {
    try {
      final NymsKeyInfo keyInfo = nymsAgent.getKeyInfoByKeyId(keyId);
      if(keyInfo.hasSecretKey() && keyInfo.isSecretKeyEncrypted()) {
        return keyInfo;
      }
    } catch (NymsAgentException e) {
      logger.warning("Error retrieving key info for "+ keyId + ": "+ e.getMessage());
    }
    return null;
  }

  private void processIncomingMessageResult(StoredMessage message, NymsIncomingProcessingResult result,
      boolean promptForPassphrase) {
    switch (result.getDecryptionResult()) {
    case DECRYPTION_FAILED:
      logger.warning("Unexpected decryption failure processing message: " + result.getFailureMessage());
      break;
    case DECRYPTION_FAILED_NO_PRIVATEKEY:
      break;
    case DECRYPTION_SUCCESS:
      processDecryptedMessage(message, result);
      break;
    case NOT_ENCRYPTED:
      break;
    case PASSPHRASE_NEEDED:
      if (promptForPassphrase) {
        processPassphrase(result.getEncryptedKeyIds(), message);
      }
    }
  }

  private void processDecryptedMessage(StoredMessage message, NymsIncomingProcessingResult result) {
    final String body = javamailUtils.getTextBody(result.getDecryptedMessage());
    final List<MessageAttachment> attachments = extractAttachments(result.getDecryptedMessage());
    message.setDecryptedMessageDetails(result.getRawDecryptedMessage(), body, attachments);
    switch (result.getSignatureVerificationResult()) {
    case NOT_SIGNED:
      message.setSignatureStatus(StoredMessage.SIGNATURE_UNSIGNED);
      break;
    case NO_VERIFY_PUBKEY:
      message.setSignatureStatus(StoredMessage.SIGNATURE_NO_PUBKEY);
      maybeSetSignatureKeyId(message, result);
      break;
    case SIGNATURE_INVALID:
      message.setSignatureStatus(StoredMessage.SIGNATURE_INVALID);
      maybeSetSignatureKeyId(message, result);
      break;
    case SIGNATURE_VALID:
      message.setSignatureStatus(StoredMessage.SIGNATURE_VALID);
      maybeSetSignatureKeyId(message, result);
      break;
    case SIGNATURE_KEY_EXPIRED:
      message.setSignatureStatus(StoredMessage.SIGNATURE_KEY_EXPIRED);
      maybeSetSignatureKeyId(message, result);
      break;
    case VERIFY_FAILED:
      logger.warning("Unexpected failure verifying signature: " + result.getFailureMessage());
      break;
    default:
      break;
    }
    try {
      searchIndex.addMessage(message);
      searchIndex.commit();
    } catch (IOException e) {
      logger.warning("I/O error indexing decrypted message: "+ e);
    }
    eventBus.post(new MessageStateChangedEvent(message));
  }
  
  private void maybeSetSignatureKeyId(StoredMessage message, NymsIncomingProcessingResult result) {
    final String keyid = result.getSignatureKeyId();
    if(keyid == null || keyid.isEmpty()) {
      return;
    }
    message.setSignatureKeyId(keyid);
  }

  private List<MessageAttachment> extractAttachments(MimeMessage decryptedMessage) {
    try {
      return javamailUtils.getAttachments(decryptedMessage);
    } catch (MessagingException | IOException e) {
      logger.log(Level.WARNING, "Error extracting attachments from decrypted message: " + e, e);
      return new ArrayList<>(0);
    }
  }
}
