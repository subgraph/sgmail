package com.subgraph.sgmail.internal.messages;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;

public class StoredMessageRawData implements Activatable {

    private final byte[] messageBytes;
    private byte[] decryptedMessageBytes;
    
	private transient Activator activator;

    public StoredMessageRawData(byte[] messageBytes) {
        this.messageBytes = messageBytes;
    }

    byte[] getMessageBytes(boolean decrypted) {
        activate(ActivationPurpose.READ);
        if(decrypted && decryptedMessageBytes != null) {
        	return decryptedMessageBytes;
        } else {
        	return messageBytes;
        }
    }
    
    void setDecryptedMessageBytes(byte[] decryptedBytes) {
    	activate(ActivationPurpose.WRITE);
    	decryptedMessageBytes = decryptedBytes;
    }
    
	@Override
	public void activate(ActivationPurpose activationPurpose) {
		if(activator != null) {
			activator.activate(activationPurpose);
		}
	}

	@Override
	public void bind(Activator activator) {
		if(this.activator == activator) {
			return;
		}
		if(activator != null  && this.activator != null) {
			throw new IllegalStateException("Object can only be bound one to an activator");
		}
		this.activator = activator;
	}

}
