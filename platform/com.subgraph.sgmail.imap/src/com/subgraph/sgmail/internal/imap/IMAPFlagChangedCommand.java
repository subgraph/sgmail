package com.subgraph.sgmail.internal.imap;

import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;

public class IMAPFlagChangedCommand implements IMAPCommand, Activatable {
    private final String folderName;
    private final long messageUID;
    private final int flag;
    private final boolean isSet;
    
	private transient Activator activator;

    IMAPFlagChangedCommand(String folderName, long messageUID, int flag, boolean isSet) {
        this.folderName = folderName;
        this.messageUID = messageUID;
        this.flag = flag;
        this.isSet = isSet;
    }

    public long getMessageUID() {
        activate(ActivationPurpose.READ);
        return messageUID;
    }

    public int getFlag() {
        activate(ActivationPurpose.READ);
        return flag;
    }

    public boolean isSet() {
        activate(ActivationPurpose.READ);
        return isSet;
    }

    @Override
    public String getFolderName() {
        activate(ActivationPurpose.READ);
        return folderName;
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
