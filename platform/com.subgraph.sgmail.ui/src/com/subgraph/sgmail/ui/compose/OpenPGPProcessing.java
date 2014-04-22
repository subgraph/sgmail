package com.subgraph.sgmail.ui.compose;

import com.subgraph.sgmail.accounts.MailAccount;
import com.subgraph.sgmail.identity.Contact;
import com.subgraph.sgmail.identity.IdentityManager;
import com.subgraph.sgmail.identity.PrivateIdentity;
import com.subgraph.sgmail.identity.PublicIdentity;
import com.subgraph.sgmail.openpgp.MessageProcessor;
import com.subgraph.sgmail.openpgp.OpenPGPException;

import org.bouncycastle.openpgp.PGPException;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.IOException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OpenPGPProcessing {
	private final MessageProcessor processor;
	private final IdentityManager identityManager;
	private final MailAccount account;
	private final MimeMessage message;
	private final boolean encrypt;
	private final boolean sign;
	private final List<PublicIdentity> publicKeys = new ArrayList<>();
	private final List<Address> missingKeys = new ArrayList<>();
	
	private MimeMessage outputMessage;
	private PrivateIdentity signingKey;
	
	OpenPGPProcessing(MessageProcessor messageProcessor, IdentityManager identityManager, MailAccount account, MimeMessage message, boolean encrypt, boolean sign) {
		this.processor = messageProcessor;
		this.identityManager = identityManager;
		this.account = account;
		this.message = message;
		this.encrypt = encrypt;
		this.sign = sign;
	}
	
	List<Address> getMissingKeyAccounts() {
		return missingKeys;
	}

	MimeMessage getOutputMessage() {
        try {
        outputMessage.saveChanges();
        } catch(MessagingException e) {
            e.printStackTrace();
        }
        return outputMessage;
	}
	boolean process() {
		try {
			if(encrypt) {
				return maybeEncrypt();
			} else if(sign) {
				return maybeSign();
			}
		} catch(MessagingException e) {
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PGPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean maybeEncrypt() throws MessagingException, IOException, PGPException, SignatureException {
		if(!generatePublicKeys()) {
			return false;
		}
		if(sign) {
			if(!findSigningKey()) {
				return false;
			}
			outputMessage = processor.signAndEncryptMessage(message, publicKeys, signingKey);
		} else {
			outputMessage = processor.encryptMessage(message, publicKeys);
		}
		return true;
	}
	
	private boolean maybeSign() throws SignatureException, MessagingException, IOException, PGPException {
		if(!findSigningKey()) {
			return false;
		}
		outputMessage = processor.signMessage(message, signingKey);
		return true;
	}
	
	private boolean findSigningKey() {
		if(account.getIdentity() != null) {
			signingKey = account.getIdentity();
		} else {
			signingKey = identityManager.findPrivateKeyByAddress(account.getEmailAddress());
		}
		if(signingKey != null) {
			try {
				signingKey.setPassphrase("");
			} catch (OpenPGPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return signingKey != null;
	}

	private boolean generatePublicKeys() throws MessagingException {
		publicKeys.clear();
		missingKeys.clear();
		
		for(Address address: message.getAllRecipients()) {
			List<PublicIdentity> keys = getPublicKeysForRecipient(address);
			if(keys.isEmpty()) {
				missingKeys.add(address);
			} else {
				publicKeys.addAll(keys);
			}
		}
		return missingKeys.isEmpty();
	}

	private List<PublicIdentity> getPublicKeysForRecipient(Address recipient) {
		if(!(recipient instanceof InternetAddress)) {
			return Collections.emptyList();
		}
        Contact c = identityManager.getContactByEmailAddress(((InternetAddress) recipient).getAddress());
        if(c.getPublicIdentity() != null) {
            List<PublicIdentity> list = new ArrayList<>(1);
            list.add(c.getPublicIdentity());
            return list;
        } else {
            return c.getLocalPublicKeys();
        }
	}
}
