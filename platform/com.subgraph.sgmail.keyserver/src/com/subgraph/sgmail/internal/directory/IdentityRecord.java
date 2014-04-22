package com.subgraph.sgmail.internal.directory;


import com.db4o.activation.ActivationPurpose;
import com.db4o.activation.Activator;
import com.db4o.ta.Activatable;

public class IdentityRecord implements Activatable {

    private final String emailAddress;
    private final byte[] keyData;

    private transient Activator activator;

    public IdentityRecord(String emailAddress, byte[] keyData) {
        this.emailAddress = emailAddress;
        this.keyData = keyData;
    }


    public String getEmailAddress() {
        activate(ActivationPurpose.READ);
        return emailAddress;
    }

    public byte[] getKeyData() {
        activate(ActivationPurpose.READ);
        return keyData;
    }

    @Override
    public void bind(Activator activator) {
        if(this.activator == activator) {
            return;
        }
        if(activator != null && this.activator != null) {
            throw new IllegalStateException("Object can only be bound to one activator");
        }
        this.activator = activator;
    }

    @Override
    public void activate(ActivationPurpose activationPurpose) {
        if(activator != null) {
            activator.activate(activationPurpose);
        }
    }
}
