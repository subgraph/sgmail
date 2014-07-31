package com.subgraph.sgmail.ui.dialogs;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.subgraph.sgmail.messages.StoredMessage;
import com.subgraph.sgmail.nyms.NymsAgent;
import com.subgraph.sgmail.nyms.NymsAgentException;
import com.subgraph.sgmail.nyms.NymsIncomingProcessingResult;
import com.subgraph.sgmail.nyms.NymsIncomingProcessingResult.DecryptionResult;
import com.subgraph.sgmail.nyms.NymsKeyInfo;
import com.subgraph.sgmail.ui.identity.PublicIdentityPane;

public class PassphraseDialog extends TitleAreaDialog {
  private final static Logger logger = Logger.getLogger(PassphraseDialog.class.getName());

  private final NymsAgent nymsAgent;
  private final List<NymsKeyInfo> keys;
  private final StoredMessage message;
  private final Session session;
  private Text passphraseText;
  private NymsIncomingProcessingResult processingResult;

  public PassphraseDialog(Shell parentShell, NymsAgent nymsAgent, List<NymsKeyInfo> keys, StoredMessage message, Session session) {
    super(parentShell);
    this.nymsAgent = nymsAgent;
    this.keys = keys;
    this.message = message;
    this.session = session;
  }

  protected Control createContents(Composite parent) {
    final Control contents = super.createContents(parent);
    setMessage("Enter passphrase to unlock private key");
    setTitle("Passphrase");
    enableOkButton(false);
    return contents;
  }

  private void enableOkButton(boolean enabled) {
    final Button button = getButton(IDialogConstants.OK_ID);
    if (button != null) {
      button.setEnabled(enabled);
    }
  }

  protected Control createDialogArea(Composite parent) {
    final Composite composite = new Composite((Composite) super.createDialogArea(parent), SWT.NONE);
    composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    composite.setLayout(new GridLayout(2, false));
    
    for(NymsKeyInfo keyInfo: keys) {
      final PublicIdentityPane publicIdentityPane = new PublicIdentityPane(composite, null, false);
      publicIdentityPane.displayKeyInfo(keyInfo);
      publicIdentityPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
    }


    final Label label = new Label(composite, SWT.NONE);
    label.setText("Enter Passphrase:");
    label.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false));

    passphraseText = new Text(composite, SWT.PASSWORD | SWT.SINGLE | SWT.BORDER);
    passphraseText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
    passphraseText.addModifyListener(createModifyListener());

    return composite;
  }

  private ModifyListener createModifyListener() {
    return new ModifyListener() {
      @Override
      public void modifyText(ModifyEvent modifyEvent) {
        enableOkButton(!passphraseText.getText().isEmpty());
      }
    };
  }

  public NymsIncomingProcessingResult getProcessingResult() {
    return processingResult;
  }
  
  private boolean testPassphrase() {
    processingResult = processMessage(passphraseText.getText());
    return processingResult == null || processingResult.getDecryptionResult() != DecryptionResult.PASSPHRASE_NEEDED;
  }
  
  private NymsIncomingProcessingResult processMessage(String passphrase) {
    try {
      final MimeMessage mimeMessage = message.toMimeMessage(session);
      return nymsAgent.processIncomingMessage(mimeMessage, passphrase);
    } catch (MessagingException e) {
      logger.log(Level.WARNING, "Error converting message to mime message: "+ e.getMessage(), e);
    } catch (NymsAgentException e) {
      logger.warning("Error processing message with nyms agent: "+ e.getMessage());
    }
    return null;
  }

  protected void okPressed() {
    if(testPassphrase()) {
      setReturnCode(OK);
      close();
      return;
    }
    passphraseText.setText("");
    setErrorMessage("Invalid passphrase");
  }
}
